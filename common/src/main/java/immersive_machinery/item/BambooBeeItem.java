package immersive_machinery.item;

import immersive_machinery.entity.BambooBee;
import immersive_machinery.entity.inventory.ContainerPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BambooBeeItem extends MachineryItem {
    public static final DataComponentType<List<ContainerPosition>> CONTAINER_POSITIONS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "container_positions", DataComponentType.<List<ContainerPosition>>builder().persistent(ContainerPosition.CODEC.listOf()).build());
    public static final DataComponentType<BambooBee.Configuration> CONFIGURATION = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "configuration", DataComponentType.<BambooBee.Configuration>builder().persistent(BambooBee.Configuration.CODEC).build());

    public BambooBeeItem(Properties settings, VehicleConstructor constructor) {
        super(settings, constructor);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, tooltips, flags);

        List<ContainerPosition> containerPositions = stack.get(CONTAINER_POSITIONS);

        if (containerPositions == null || containerPositions.isEmpty()) {
            tooltips.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.help1"));
            tooltips.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.help2"));
        } else {
            tooltips.add(Component.translatable("gui.immersive_machinery.bamboo_bee.tooltip.header", containerPositions.size()));

            for (ContainerPosition containerPosition : containerPositions) {
                String key = "gui.immersive_machinery.bamboo_bee.tooltip." + (containerPosition.input() ? "input" : "output");
                tooltips.add(
                        Component.translatable(key, Component.translatable(containerPosition.name())).withStyle(containerPosition.input() ? ChatFormatting.GREEN : ChatFormatting.AQUA)
                );
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        if (context.isSecondaryUseActive()) {
            if (!context.getLevel().isClientSide()) {
                if (blockEntity instanceof Container) {
                    String name = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toLanguageKey("block");
                    recordPosition(context.getItemInHand(), context.getClickedPos(), context.getPlayer(), name);
                } else {
                    context.getItemInHand().remove(CONTAINER_POSITIONS);
                    send("positions_cleared", context.getPlayer(), ChatFormatting.GOLD);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(context);
        }
    }

    private void recordPosition(ItemStack stack, BlockPos pos, Player player, String name) {
        get(stack, pos).ifPresentOrElse(
                p -> {
                    if (p.input()) {
                        flip(stack, pos);
                        send("position_output", player, ChatFormatting.GREEN);
                    } else {
                        remove(stack, pos);
                        send("position_removed", player, ChatFormatting.GRAY);
                    }
                },
                () -> {
                    add(stack, pos, name);
                    send("position_input", player, ChatFormatting.AQUA);
                }
        );
    }

    private void send(String message, Player player, ChatFormatting formatting) {
        if (player != null) {
            player.sendSystemMessage(Component.translatable("gui.immersive_machinery.bamboo_bee." + message).withStyle(formatting));
        }
    }

    private void add(ItemStack stack, BlockPos pos, String name) {
        List<ContainerPosition> containerPositions = stack.getOrDefault(CONTAINER_POSITIONS, new LinkedList<>());
        containerPositions = new LinkedList<>(containerPositions);
        containerPositions.add(new ContainerPosition(pos, name, true));
        stack.set(CONTAINER_POSITIONS, containerPositions);
    }

    private Optional<ContainerPosition> get(ItemStack stack, BlockPos pos) {
        List<ContainerPosition> containerPositions = stack.getOrDefault(CONTAINER_POSITIONS, new LinkedList<>());
        for (ContainerPosition p : containerPositions) {
            if (p.getPos() == pos.asLong()) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    private void flip(ItemStack stack, BlockPos pos) {
        List<ContainerPosition> containerPositions = stack.getOrDefault(CONTAINER_POSITIONS, new LinkedList<>());
        containerPositions = new LinkedList<>(containerPositions);

        for (int i = 0; i < containerPositions.size(); i++) {
            ContainerPosition p = containerPositions.get(i);
            if (p.getPos() == pos.asLong()) {
                containerPositions.set(i, new ContainerPosition(pos, p.name(), !p.input()));
                stack.set(CONTAINER_POSITIONS, containerPositions);
                return;
            }
        }
    }

    private void remove(ItemStack stack, BlockPos pos) {
        List<ContainerPosition> containerPositions = stack.getOrDefault(CONTAINER_POSITIONS, new LinkedList<>());
        containerPositions = new LinkedList<>(containerPositions);
        for (ContainerPosition p : containerPositions) {
            if (p.getPos() == pos.asLong()) {
                containerPositions.remove(p);
                stack.set(CONTAINER_POSITIONS, containerPositions);
                return;
            }
        }
    }
}
