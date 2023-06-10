package io.github.yuko1101.afktoinvincible.mixin.piston;

import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        // server side
        if (AfkToInvincibleServer.INSTANCE != null) {
            final Box movedTo = state.getCollisionShape(world, pos.offset(direction)).getBoundingBox();

            final boolean isAfkPlayerIn = world.getPlayers().stream().anyMatch(player -> {
                if (!AfkToInvincible.isAfk(player.getUuid(), false)) return false;
                final Box boundingBox = player.getBoundingBox();
                return boundingBox.intersects(movedTo);
            });

            if (isAfkPlayerIn) cir.setReturnValue(false);
        }
    }
}
