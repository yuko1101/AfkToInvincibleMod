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

import java.util.List;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {

    // TODO: ピストンの押し出し部によってAFK中のプレイヤーが押されないようにする

    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        // server side
        if (AfkToInvincibleServer.INSTANCE != null) {
            final List<Box> movedFrom = state.getCollisionShape(world, pos).getBoundingBoxes().stream().map(box -> box.offset(pos)).toList();
            final List<Box> movedTo = state.getCollisionShape(world, pos).getBoundingBoxes().stream().map(box -> box.offset(pos.offset(direction))).toList();
            AfkToInvincible.LOGGER.info("pos: " + pos);
            AfkToInvincible.LOGGER.info("movedFrom: " + movedFrom);
            AfkToInvincible.LOGGER.info("movedTo: " + movedTo);

            final boolean isAfkPlayerIn = world.getPlayers().stream().anyMatch(player -> {
                if (!AfkToInvincible.isAfk(player.getUuid(), false)) return false;
                final Box playerBoundingBox = player.getBoundingBox();
                AfkToInvincible.LOGGER.info("player: " + playerBoundingBox.toString());

                // 動くブロックの上に放置している人がいる場合、ピストンが動かないようにする

                // 動く元の位置のブロックの当たり判定の上面がプレイヤーの当たり判定の下面と少しでも重なっている場合
                if (movedFrom.stream().anyMatch(box -> touches(box, playerBoundingBox))) {

                    // プレイヤーの乗っているブロック(空気も含む)の座標
                    final BlockPos posUnder = new BlockPos(player.getBlockX(), (int) (Math.ceil(player.getY()) - 1), player.getBlockZ());

                    // 重心が動くブロックに乗っているため、押せなくする
                    if (posUnder.equals(pos)) return true;

                    // プレイヤーの乗っているブロックの当たり判定
                    final BlockState blockUnder = world.getBlockState(posUnder);
                    final List<Box> blockShapeUnder = blockUnder.getCollisionShape(world, posUnder).getBoundingBoxes().stream().map(box -> box.offset(posUnder)).toList();

                    // blockShapeUnderの上面とプレイヤーの当たり判定の下面が触れいていない場合、動くブロックに重心が乗っている可能性が出てくるため、動かなくする
                    if (blockShapeUnder.stream().noneMatch(box -> touches(box, playerBoundingBox))) return true;
                }

                return movedTo.stream().anyMatch(playerBoundingBox::intersects);
            });

            AfkToInvincible.LOGGER.info("isAfkPlayerIn: " + isAfkPlayerIn);

            if (isAfkPlayerIn) cir.setReturnValue(false);
        }
    }

    private static boolean touches(Box below, Box above) {
        return below.maxY == above.minY && below.minX <= above.maxX && below.maxX >= above.minX && below.minZ <= above.maxZ && below.maxZ >= above.minZ;
    }
}
