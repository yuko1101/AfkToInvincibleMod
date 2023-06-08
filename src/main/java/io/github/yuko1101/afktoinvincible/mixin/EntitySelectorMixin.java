package io.github.yuko1101.afktoinvincible.mixin;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
    @Shadow
    public static Predicate<Entity> pushableBy(Entity entity) {
        return null;
    }

    private static boolean isOriginal = false;

    @Inject(method = "pushableBy", at = @At("HEAD"), cancellable = true)
    private static void pushableBy(Entity entity, CallbackInfoReturnable<Predicate<Entity>> ci) {
        if (isOriginal) {
            isOriginal = false;
            return;
        }
        isOriginal = true;
        final Predicate<Entity> originalPredicate = Objects.requireNonNull(pushableBy(entity));
        ci.setReturnValue(entity2 -> {
            if (AfkToInvincible.INSTANCE.isAfk(entity.getUUID()) || AfkToInvincible.INSTANCE.isAfk(entity2.getUUID())) {
                AfkToInvincible.LOGGER.info("afk pushable by");
                return false;
            }
            AfkToInvincible.LOGGER.info("original pushable by");
            return originalPredicate.test(entity2);
        });
    }
}
