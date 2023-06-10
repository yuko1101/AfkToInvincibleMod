package io.github.yuko1101.afktoinvincible.client;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

import static io.github.yuko1101.afktoinvincible.AfkToInvincible.AFK_PACKET_ID;

public class AfkToInvincibleClient implements ClientModInitializer {

    public static AfkToInvincibleClient INSTANCE;

    public boolean isAfkClient = false;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        registerPacketReceiver();
    }

    public boolean isAfk(UUID uuid) {
        final PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getUuid() != uuid) return false;
        AfkToInvincible.LOGGER.info(String.valueOf(isAfkClient));
        return isAfkClient;
    }

    public void registerPacketReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(AFK_PACKET_ID, (client, handler, buf, responseSender) -> {
            isAfkClient = buf.readBoolean();
        });
    }
}
