package io.github.yuko1101.afktoinvincible;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AfkToInvincible implements ModInitializer {

    public static final String MOD_ID = "afk_to_invincible";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static AfkToInvincible INSTANCE;

    public static final int AFK_TICKS = 20 * 5;

    public static final HashMap<UUID, Integer> afkTicksMap = new HashMap<>();
    public static final HashMap<UUID, Pair<Vec3, Vec2>> lastStateMap = new HashMap<>();

    @Override
    public void onInitialize() {
        INSTANCE = this;
        LOGGER.info("AfkToInvincible initialized");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            final List<ServerPlayer> players = server.getPlayerList().getPlayers();
            players.forEach(player -> {
                if (!afkTicksMap.containsKey(player.getUUID())) {
                    afkTicksMap.put(player.getUUID(), 0);
                }
                boolean isMoved = false;
                if (lastStateMap.containsKey(player.getUUID())) {
                    final Pair<Vec3, Vec2> lastState = lastStateMap.get(player.getUUID());
                    final Vec3 lastPos = lastState.getFirst();
                    final Vec2 lastCameraDirection = lastState.getSecond();
                    if (!player.position().equals(lastPos)) isMoved = true;
                    if (!player.getRotationVector().equals(lastCameraDirection)) isMoved = true;
                }

                if (!isMoved) {
                    final int currentAfkTime = afkTicksMap.get(player.getUUID());
                    if (currentAfkTime < AFK_TICKS) afkTicksMap.put(player.getUUID(), currentAfkTime + 1);
                } else {
                    afkTicksMap.put(player.getUUID(), 0);
                }
                updateInvincible(player, afkTicksMap.get(player.getUUID()));
                lastStateMap.put(player.getUUID(), new Pair<>(player.position(), player.getRotationVector()));
            });
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof Player && getAfkTicks(entity.getUUID()) >= AFK_TICKS));
    }

    private void updateInvincible(ServerPlayer player, int afkTicks) {
//        if (afkTicks % 20 == 0) LOGGER.info(String.valueOf(afkTicks));
//        if (afkTicks >= AFK_TICKS) {
//            player.setInvulnerable(false);
//            Objects.requireNonNull(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(100000);
//        } else {
//            Objects.requireNonNull(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0);
//        }
    }

    public int getAfkTicks(UUID uuid) {
        if (!afkTicksMap.containsKey(uuid)) return 0;
        return afkTicksMap.get(uuid);
    }

    public boolean isAfk(UUID uuid) {
        return getAfkTicks(uuid) >= AFK_TICKS;
    }
}
