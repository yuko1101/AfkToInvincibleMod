package io.github.yuko1101.afktoinvincible.mixin;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (AfkToInvincible.isAfk(getUuid(), getWorld().isClient)) {
            cir.setReturnValue(false);
        }
    }
}
