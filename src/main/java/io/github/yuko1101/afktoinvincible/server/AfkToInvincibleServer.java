package io.github.yuko1101.afktoinvincible.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import io.github.yuko1101.afktoinvincible.commands.AfkCommand;
import io.github.yuko1101.afktoinvincible.commands.AfkConfigCommand;
import io.github.yuko1101.afktoinvincible.utils.ConfigFile;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static io.github.yuko1101.afktoinvincible.AfkToInvincible.*;

public class AfkToInvincibleServer implements DedicatedServerModInitializer {

    public static AfkToInvincibleServer INSTANCE;

    public static ConfigFile configFile;

    public static final HashMap<UUID, Integer> afkTicksMap = new HashMap<>();
    public static final HashMap<UUID, Pair<Vec3d, Vec2f>> lastStateMap = new HashMap<>();

    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        INSTANCE = this;

        try {
            configFile = new ConfigFile(new File("config/afk_to_invincible.json"), new JsonObject()).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AFK_TICKS = configFile.has("afkTimeout") ? configFile.getValue("afkTimeout").getAsInt() : AFK_TICKS;

        CommandRegistrationCallback.EVENT.register(new AfkCommand()::register);
        CommandRegistrationCallback.EVENT.register(new AfkConfigCommand()::register);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            final List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            players.forEach(player -> {
                if (!isAfkEnabled(player.getUuid())) {
                    afkTicksMap.remove(player.getUuid());
                    lastStateMap.remove(player.getUuid());
                    return;
                }
                if (!afkTicksMap.containsKey(player.getUuid())) {
                    afkTicksMap.put(player.getUuid(), 0);
                    updateInvincible(player, false, true);
                } else {
                    final int preAfkTicks = afkTicksMap.get(player.getUuid());
                    int newAfkTicks = preAfkTicks;

                    boolean isMoved = false;
                    if (lastStateMap.containsKey(player.getUuid())) {
                        final Pair<Vec3d, Vec2f> lastState = lastStateMap.get(player.getUuid());
                        final Vec3d lastPos = lastState.getFirst();
                        final Vec2f lastCameraDirection = lastState.getSecond();
                        if (!player.getPos().equals(lastPos)) isMoved = true;
                        if (!player.getRotationClient().equals(lastCameraDirection)) isMoved = true;
                    }

                    if (!isMoved) {
                        if (newAfkTicks < AFK_TICKS) newAfkTicks++;
                    } else {
                        newAfkTicks = 0;
                    }

                    if (newAfkTicks < AFK_TICKS && preAfkTicks >= AFK_TICKS) {
                        updateInvincible(player, false, false);
                    } else if (newAfkTicks >= AFK_TICKS && preAfkTicks < AFK_TICKS) {
                        updateInvincible(player, true, false);
                    }

                    afkTicksMap.put(player.getUuid(), newAfkTicks);
                }

                lastStateMap.put(player.getUuid(), new Pair<>(player.getPos(), player.getRotationClient()));
            });
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            sendAfkTimeoutPacketFromServer(server.getPlayerManager().getPlayerList(), AFK_TICKS);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sendAfkTimeoutPacketFromServer(List.of(handler.getPlayer()), AFK_TICKS);
        });

        AfkToInvincible.LOGGER.info("AfkToInvincible initialized for server");
    }

    public void updateInvincible(ServerPlayerEntity player, boolean isAfk, boolean isInitialUpdate) {
        sendIsAfkPacketFromServer(player, isAfk);
        player.setInvulnerable(isAfk);
        player.setNoGravity(isAfk);
        if (!isInitialUpdate) {
            final String message = (isAfk ? AFK_ENABLED_MESSAGE : AFK_DISABLED_MESSAGE).replaceAll("%player%", player.getName().getString());
            player.server.getPlayerManager().getPlayerList().forEach(p -> p.sendMessage(Text.literal(message)));
        }
    }

    public int getAfkTicks(UUID uuid) {
        if (!afkTicksMap.containsKey(uuid)) return 0;
        return afkTicksMap.get(uuid);
    }

    public boolean isAfk(UUID uuid) {
        return getAfkTicks(uuid) >= AFK_TICKS && isAfkEnabled(uuid);
    }

    public List<ServerPlayerEntity> getAfkPlayers() {
        return afkTicksMap.keySet().stream().filter(this::isAfk).map(server.getPlayerManager()::getPlayer).toList();
    }

    public void sendIsAfkPacketFromServer(ServerPlayerEntity player, boolean isAfk) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isAfk);

        ServerPlayNetworking.send(player, AFK_PACKET_ID, buf);
    }

    public void sendAfkTimeoutPacketFromServer(List<ServerPlayerEntity> players, int timeout) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(timeout);

        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, AFK_TIMEOUT_PACKET_ID, buf);
        }
    }

    public boolean isAfkEnabled(UUID uuid) {
        try {
            return configFile.get("players").get(uuid.toString()).getValue("isEnabled").getAsBoolean();
        } catch (Exception e) {
            return true; // enabled by default
        }
    }

    public void setAfkEnabled(ServerPlayerEntity player, boolean isEnabled) {
        if (!configFile.has("players")) configFile.set("players", new JsonObject());
        if (!configFile.get("players").has(player.getUuid().toString())) configFile.get("players").set(player.getUuid().toString(), new JsonObject());

        boolean wasAfk = isAfk(player.getUuid());
        configFile.get("players").get(player.getUuid().toString()).set("isEnabled", isEnabled);
        boolean isAfk = isAfk(player.getUuid());
        if (wasAfk != isAfk) updateInvincible(player, isAfk, false);

        try {
            configFile.save();
        } catch (Exception ignored) { }


    }
}
