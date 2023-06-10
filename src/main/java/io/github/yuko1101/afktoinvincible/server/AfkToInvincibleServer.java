package io.github.yuko1101.afktoinvincible.server;

import com.mojang.datafixers.util.Pair;
import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static io.github.yuko1101.afktoinvincible.AfkToInvincible.AFK_PACKET_ID;
import static io.github.yuko1101.afktoinvincible.AfkToInvincible.AFK_TICKS;

public class AfkToInvincibleServer implements DedicatedServerModInitializer {

    public static AfkToInvincibleServer INSTANCE;

    public static final HashMap<UUID, Integer> afkTicksMap = new HashMap<>();
    public static final HashMap<UUID, Pair<Vec3d, Vec2f>> lastStateMap = new HashMap<>();

    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        INSTANCE = this;
        AfkToInvincible.LOGGER.info("AfkToInvincible initialized");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            final List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            players.forEach(player -> {
                if (!afkTicksMap.containsKey(player.getUuid())) {
                    afkTicksMap.put(player.getUuid(), 0);
                }
                boolean isMoved = false;
                if (lastStateMap.containsKey(player.getUuid())) {
                    final Pair<Vec3d, Vec2f> lastState = lastStateMap.get(player.getUuid());
                    final Vec3d lastPos = lastState.getFirst();
                    final Vec2f lastCameraDirection = lastState.getSecond();
                    if (!player.getPos().equals(lastPos)) isMoved = true;
                    if (!player.getRotationClient().equals(lastCameraDirection)) isMoved = true;
                }

                if (!isMoved) {
                    int currentAfkTime = afkTicksMap.get(player.getUuid());
                    if (currentAfkTime < AFK_TICKS) afkTicksMap.put(player.getUuid(), ++currentAfkTime);
                    if (currentAfkTime == AFK_TICKS) updateInvincible(player, true);

                } else {
                    afkTicksMap.put(player.getUuid(), 0);
                    updateInvincible(player, false);
                }
                lastStateMap.put(player.getUuid(), new Pair<>(player.getPos(), player.getRotationClient()));
            });
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
        });
    }

    private void updateInvincible(ServerPlayerEntity player, boolean isAfk) {
        sendPacketFromServer(player, isAfk);
        player.setInvulnerable(isAfk);
        player.setNoGravity(isAfk);
    }

    public int getAfkTicks(UUID uuid) {
        if (!afkTicksMap.containsKey(uuid)) return 0;
        return afkTicksMap.get(uuid);
    }

    public boolean isAfk(UUID uuid) {
        return getAfkTicks(uuid) >= AFK_TICKS;
    }

    public void sendPacketFromServer(ServerPlayerEntity player, boolean isAfk) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isAfk);

        ServerPlayNetworking.send(player, AFK_PACKET_ID, buf);
    }
}
