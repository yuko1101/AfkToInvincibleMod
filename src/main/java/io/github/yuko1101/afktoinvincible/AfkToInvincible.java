package io.github.yuko1101.afktoinvincible;

import com.mojang.datafixers.util.Pair;
import io.github.yuko1101.afktoinvincible.client.AfkToInvincibleClient;
import io.github.yuko1101.afktoinvincible.commands.AfkCommand;
import io.github.yuko1101.afktoinvincible.commands.AfkConfigCommand;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class AfkToInvincible implements ModInitializer {

    public static final String MOD_ID = "afk_to_invincible";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int AFK_TICKS = 20 * 5;

    public static final Identifier AFK_PACKET_ID = new Identifier(MOD_ID, "is_afk");
    public static final Identifier AFK_TIMEOUT_PACKET_ID = new Identifier(MOD_ID, "afk_timeout");

    public static final String AFK_ENABLED_MESSAGE = "%player%さんが放置状態になりました。";
    public static final String AFK_DISABLED_MESSAGE = "%player%さんが放置状態から解除されました。";

    public static AfkToInvincible INSTANCE;

    public final AfkToInvincibleServer serverManager = new AfkToInvincibleServer();

    @Override
    public void onInitialize() {

        INSTANCE = this;

        serverManager.init();
    }

    public static boolean isAfk(Entity entity) {
        return isAfk(entity.getUuid(), entity.getWorld().isClient);
    }

    public static boolean isAfk(UUID uuid, boolean isClientSide) {
        if (isClientSide) {
            return AfkToInvincibleClient.INSTANCE.isAfk(uuid);
        } else {
            return INSTANCE.serverManager.isAfk(uuid);
        }
    }
}
