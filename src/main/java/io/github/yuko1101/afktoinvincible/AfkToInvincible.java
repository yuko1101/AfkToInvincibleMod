package io.github.yuko1101.afktoinvincible;

import io.github.yuko1101.afktoinvincible.client.AfkToInvincibleClient;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AfkToInvincible implements ModInitializer {

    public static final String MOD_ID = "afk_to_invincible";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int AFK_TICKS = 20 * 5;

    public static final Identifier AFK_PACKET_ID = new Identifier(MOD_ID, "is_afk");
    public static final Identifier AFK_TIMEOUT_PACKET_ID = new Identifier(MOD_ID, "afk_timeout");

    public static final String AFK_ENABLED_MESSAGE = "%player%さんが放置状態になりました。";
    public static final String AFK_DISABLED_MESSAGE = "%player%さんが放置状態から解除されました。";

    @Override
    public void onInitialize() {

    }

    public static boolean isAfk(Entity entity) {
        return isAfk(entity.getUuid(), entity.getWorld().isClient);
    }

    public static boolean isAfk(UUID uuid, boolean isClientSide) {
        if (isClientSide) {
            return AfkToInvincibleClient.INSTANCE.isAfk(uuid);
        } else {
            return AfkToInvincibleServer.INSTANCE != null && AfkToInvincibleServer.INSTANCE.isAfk(uuid);
        }
    }
}
