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

import java.util.ArrayList;
import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        // server side
        if (AfkToInvincibleServer.INSTANCE != null) {
            // TODO: それぞれのboxをピストンの向きと反対方向に移動させたboxとunionを使って統合させることで、ピストンの動きによる当たり判定の伸び(当たり判定の移動を考えたときの当たり判定)を無理やり処理している。もっと良い方法を考える。
            final List<Box> movedTo = new ArrayList<>(state.getCollisionShape(world, pos).getBoundingBoxes().stream().map(box -> box.union(box.offset(new BlockPos(0, 0, 0).offset(direction.getOpposite())))).map(box -> box.offset(pos.offset(direction))).toList());
            // TODO: ピストンの押し出し部の当たり判定が何故かなくなったりするため、当たり判定を無理やり作っていが、もっと良い方法を考える。
            if (movedTo.isEmpty()) {
                movedTo.add(new Box(pos));
            }
//            AfkToInvincible.LOGGER.info("movedTo: " + movedTo);

            final boolean isAfkPlayerIn = world.getPlayers().stream().anyMatch(player -> {
                if (!AfkToInvincible.isAfk(player.getUuid(), false)) return false;
                final Box boundingBox = player.getBoundingBox();
//                AfkToInvincible.LOGGER.info("player: " + boundingBox);
                return movedTo.stream().anyMatch(boundingBox::intersects);
            });

//            AfkToInvincible.LOGGER.info("isAfkPlayerIn: " + isAfkPlayerIn);

            if (isAfkPlayerIn) cir.setReturnValue(false);
        }
    }
}
