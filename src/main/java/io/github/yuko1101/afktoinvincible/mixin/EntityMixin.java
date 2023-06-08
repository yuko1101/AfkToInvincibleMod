package io.github.yuko1101.afktoinvincible.mixin;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract UUID getUUID();

    @Inject(method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void setDeltaMovement(Vec3 vec3, CallbackInfo ci) {
        if (AfkToInvincible.INSTANCE.isAfk(this.getUUID())) {
            AfkToInvincible.LOGGER.info("cancel movement");
            ci.cancel();
        } else {
            AfkToInvincible.LOGGER.info("movement");
        }
    }
}
