package immersive_machinery.item;

import immersive_machinery.entity.BambooBee;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BambooBeeItem extends MachineryItem {
    public static final String TAG = "ContainerPositions";

    public BambooBeeItem(Properties settings, VehicleConstructor constructor) {
        super(settings, constructor);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        ListTag positions = getPositions(stack);

        if (positions.isEmpty()) {
            tooltip.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.help1"));
            tooltip.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.help2"));
        } else {
            tooltip.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.header", positions.size()));

            for (int i = 0; i < positions.size(); i++) {
                CompoundTag tag = positions.getCompound(i);
                String name = tag.getString("name");
                boolean input = tag.getBoolean("input");

                tooltip.add(
                        Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip." + (input ? "input" : "output"), name)
                                .withStyle(input ? ChatFormatting.GREEN : ChatFormatting.AQUA)
                );
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        if (context.isSecondaryUseActive()) {
            if (!context.getLevel().isClientSide()) {
                if (blockEntity instanceof Container container) {
                    recordPosition(context.getItemInHand(), context.getClickedPos(), context.getPlayer(), container.toString());
                } else {
                    getPositions(context.getItemInHand()).clear();
                    send("positions_cleared", context.getPlayer(), ChatFormatting.GOLD);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(context);
        }
    }

    private void recordPosition(ItemStack stack, BlockPos pos, Player player, String container) {
        get(stack, pos).ifPresentOrElse(
                containerTag -> {
                    if (containerTag.getBoolean("input")) {
                        remove(stack, pos);
                        send("position_removed", player, ChatFormatting.GRAY);
                    } else {
                        containerTag.putBoolean("input", true);
                        send("position_output", player, ChatFormatting.GREEN);
                    }
                },
                () -> {
                    add(stack, pos, container);
                    send("position_input", player, ChatFormatting.AQUA);
                }
        );
    }

    private void send(String message, Player player, ChatFormatting formatting) {
        if (player != null) {
            player.sendSystemMessage(Component.translatable("gui.immersive_machinery.bamboo_bee." + message).withStyle(formatting));
        }
    }

    private ListTag getPositions(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG)) {
            tag.put(TAG, new ListTag());
        }
        return tag.getList(TAG, 10);
    }

    private void add(ItemStack stack, BlockPos pos, String name) {
        CompoundTag tag = new BambooBee.ContainerPosition(pos, name, false).toTag();
        getPositions(stack).add(tag);
    }

    private Optional<CompoundTag> get(ItemStack stack, BlockPos pos) {
        ListTag list = getPositions(stack);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (tag.getLong("pos") == pos.asLong()) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

    private void remove(ItemStack stack, BlockPos pos) {
        ListTag list = getPositions(stack);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (tag.getLong("pos") == pos.asLong()) {
                list.remove(i);
                return;
            }
        }
    }
}
