package immersive_machinery;

import immersive_machinery.entity.MachineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

import static net.minecraft.world.level.block.Block.popResource;

public class Utils {
    public static BlockPos doubleToPos(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public static float mineBlock(ServerLevel level, BlockPos pos, MachineEntity vehicle) {
        BlockState blockState = level.getBlockState(pos);
        float destroySpeed = blockState.getDestroySpeed(level, pos);
        if (!blockState.isAir() && destroySpeed != -1.0f) {
            FluidState fluidState = level.getFluidState(pos);
            if (!(blockState.getBlock() instanceof BaseFireBlock)) {
                level.levelEvent(2001, pos, Block.getId(blockState));
            }

            // Mine the block
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.getDrops(blockState, level, pos, blockEntity, vehicle, ItemStack.EMPTY).forEach(itemStack -> {
                if (vehicle.getInventory().canAddItem(itemStack)) {
                    itemStack = vehicle.addItem(itemStack);
                }
                if (!itemStack.isEmpty()) {
                    popResource(level, pos, itemStack);
                }
            });
            blockState.spawnAfterBreak(level, pos, ItemStack.EMPTY, false);

            boolean replaced = level.setBlock(pos, fluidState.createLegacyBlock(), 3, 512);
            if (replaced) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(vehicle, blockState));
            }
        }
        return Math.max(destroySpeed, 0.0f);
    }
}
