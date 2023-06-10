package io.github.yuko1101.afktoinvincible.mixin;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract World getWorld();

    @Shadow public abstract UUID getUuid();

    @Inject(method = "isImmuneToExplosion", at = @At("HEAD"), cancellable = true)
    private void isImmuneToExplosion(CallbackInfoReturnable<Boolean> cir) {
        if (AfkToInvincible.isAfk(getUuid(), getWorld().isClient)) {
            cir.setReturnValue(true);
        }
    }
}
