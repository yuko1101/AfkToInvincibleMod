package io.github.yuko1101.afktoinvincible.mixin;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
    private void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if (AfkToInvincible.isAfk(getUuid(), getWorld().isClient)) {
            cir.setReturnValue(false);
        }
    }
}
