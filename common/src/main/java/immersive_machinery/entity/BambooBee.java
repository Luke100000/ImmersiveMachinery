package immersive_machinery.entity;

import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.Common;
import immersive_machinery.Items;
import immersive_machinery.Utils;
import immersive_machinery.entity.misc.PilotNavigator;
import immersive_machinery.item.BambooBeeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BambooBee extends MachineEntity {
    private Configuration configuration = new Configuration();
    private final List<ContainerPosition> containerPositions = new LinkedList<>();
    private Task currentTask;
    private final PilotNavigator navigator;
    private int searchCooldown = 0;

    public static final int WORK_SLOT = 0;

    public BambooBee(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, false);

        navigator = new PilotNavigator(this, true);
    }

    @Override
    protected float getGravity() {
        return (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            navigator.tick();

            if (currentTask == null) {
                if (searchCooldown > 0) {
                    searchCooldown--;
                } else {
                    currentTask = getTask();

                    if (currentTask == null) {
                        searchCooldown = 60;
                    }
                }
            } else {
                ItemStack carries = getSlot(WORK_SLOT).get();
                if (carries.isEmpty()) {
                    // Fetch item
                    if (moveTo(currentTask.source().above())) {
                        BlockEntity blockEntity = level().getBlockEntity(currentTask.source());
                        if (blockEntity instanceof Container container) {
                            ItemStack item = container.getItem(currentTask.slot());
                            if (match(item, currentTask.stack())) {
                                ItemStack stack = container.removeItem(currentTask.slot(), item.getCount());
                                getSlot(WORK_SLOT).set(stack);
                            } else {
                                error("Item mismatches!");
                                currentTask = null;
                            }
                        } else {
                            error("Container gone!");
                            currentTask = null;
                        }
                    }
                } else if (match(carries, currentTask.stack())) {
                    // Deposit item
                    if (moveTo(currentTask.target().above())) {
                        BlockEntity blockEntity = level().getBlockEntity(currentTask.target());
                        if (blockEntity instanceof Container container) {
                            addToContainer(container, carries);
                            if (carries.isEmpty()) {
                                currentTask = null;
                            } else {
                                returnItem();
                            }
                        } else {
                            error("Container gone!");
                            currentTask = null;
                        }
                    }
                } else {
                    // Wrong item, return to source
                    error("Wrong item, returning to source!");
                    returnItem();
                }
            }
        }

        // Rotate to direction
        if (level().isClientSide) {
            double dx = x - secondLastX;
            double dy = y - secondLastY;
            double dz = z - secondLastZ;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > 0.0f) {
                float yRot = getYRot();
                setXRot(Utils.lerpAngle(getXRot(), (float) (dy * 90.0f), 5.0f));
                setYRot(Utils.lerpAngle(yRot, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, 10.0f));
                setZRot(Utils.lerpAngle(getRoll(), (getYRot() - yRot) * 2.0f, 2.0f));
            } else {
                setXRot(Utils.lerpAngle(getXRot(), 0.0f, 10.0f));
                setZRot(Utils.lerpAngle(getRoll(), 0.0f, 10.0f));
            }
        }

        setEngineTarget(currentTask != null ? 1.0f : 0.0f);
    }

    @Override
    public boolean isVehicle() {
        return true;
    }

    @Override
    protected void handleNetherPortal() {
        // Do nothing
    }

    private void addToContainer(Container container, ItemStack carries) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (container.canPlaceItem(slot, carries)) {
                if (ItemStack.isSameItemSameTags(carries, stack)) {
                    int count = Math.min(carries.getCount(), stack.getMaxStackSize() - stack.getCount());
                    stack.grow(count);
                    carries.shrink(count);
                    container.setItem(slot, stack);
                } else if (stack.isEmpty()) {
                    container.setItem(slot, carries.copyAndClear());
                }
            }
        }
    }

    private void returnItem() {
        ItemStack carries = getSlot(WORK_SLOT).get();
        currentTask = new Task(currentTask.source(), currentTask.slot(), carries.copy(), currentTask.source());
    }

    private void error(String message) {
        MutableComponent error = Component.translatable("entity.immersive_machinery.bamboo_bee")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD)
                .append(": ")
                .append(Component.translatable(message));
        level().players().stream()
                .filter(player -> player.distanceToSqr(this) < 64)
                .forEach(player -> player.displayClientMessage(error, false));
    }

    private boolean moveTo(BlockPos pos) {
        navigator.moveTo(pos);
        Vec3 center = pos.getCenter();
        return Math.max(
                Math.max(
                        Math.abs(center.x() - getX()),
                        Math.abs(center.y() - getY())
                ),
                Math.abs(center.z() - getZ())
        ) < 1.5;
    }

    // todo add round robin
    private List<ContainerPosition> getPositions(boolean input) {
        List<ContainerPosition> positions = new LinkedList<>();
        for (ContainerPosition position : containerPositions) {
            if (position.input() == input) {
                positions.add(position);
            }
        }
        return positions;
    }

    private Task getTask() {
        for (ContainerPosition position : getPositions(true)) {
            BlockEntity blockEntity = level().getBlockEntity(position.pos());
            if (blockEntity instanceof Container container) {
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    ItemStack item = container.getItem(slot);
                    if (!item.isEmpty() && container.canTakeItem(container, slot, item) && filter(item)) {
                        Task task = findTask(position.pos(), slot, item);
                        if (task != null) {
                            return task;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Task findTask(BlockPos source, int slot, ItemStack item) {
        for (ContainerPosition position : getPositions(false)) {
            BlockEntity blockEntity = level().getBlockEntity(position.pos());
            if (blockEntity instanceof Container container) {
                if (couldMove(item, container)) {
                    return new Task(source, slot, item.copy(), position.pos());
                }
            }
        }
        return null;
    }

    private boolean couldMove(ItemStack item, Container container) {
        int count = item.getCount();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (container.canPlaceItem(slot, item)) {
                if (ItemStack.isSameItemSameTags(item, stack)) {
                    count -= (stack.getMaxStackSize() - stack.getCount());
                } else if (stack.isEmpty()) {
                    count -= item.getMaxStackSize();
                }

                if (count <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean filter(ItemStack stack) {
        List<ItemStack> slots = getSlots(Common.SLOT_FILTER);
        return isEmpty(slots) || slots.stream().anyMatch(other -> match(stack, other));
    }

    private boolean isEmpty(List<ItemStack> slots) {
        return slots.stream().allMatch(ItemStack::isEmpty);
    }

    private boolean match(ItemStack stack, ItemStack other) {
        if (configuration.compareTag) {
            return ItemStack.isSameItemSameTags(stack, other);
        } else {
            return ItemStack.isSameItem(stack, other);
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeConfiguration(tag);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readConfiguration(tag);
    }

    @Override
    protected void addItemTag(@NotNull CompoundTag tag) {
        super.addItemTag(tag);
        writeConfiguration(tag);
    }

    @Override
    protected void readItemTag(@NotNull CompoundTag tag) {
        super.readItemTag(tag);
        readConfiguration(tag);
    }

    private void readConfiguration(CompoundTag tag) {
        // Read container positions
        if (tag.contains(BambooBeeItem.TAG)) {
            ListTag list = tag.getList(BambooBeeItem.TAG, 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag containerTag = list.getCompound(i);
                containerPositions.add(ContainerPosition.fromTag(containerTag));
            }
        }

        configuration.read(tag);
    }

    private void writeConfiguration(CompoundTag tag) {
        // Save container positions
        ListTag list = new ListTag();
        for (ContainerPosition position : containerPositions) {
            list.add(position.toTag());
        }
        tag.put(BambooBeeItem.TAG, list);

        configuration.write(tag);
    }

    @Override
    public void setAnimationVariables(float tickDelta) {
        super.setAnimationVariables(tickDelta);

        BBAnimationVariables.set("grabber", getInventory().getItem(WORK_SLOT).isEmpty() ? -45.0f : 0.0f);
    }

    @Override
    public Item asItem() {
        return Items.BAMBOO_BEE.get();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public record Task(BlockPos source, int slot, ItemStack stack, BlockPos target) {
    }

    public record ContainerPosition(BlockPos pos, String name, boolean input) {
        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("pos", getPos());
            tag.putString("name", name());
            tag.putBoolean("input", input());
            return tag;
        }

        public static ContainerPosition fromTag(CompoundTag tag) {
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            String name = tag.getString("name");
            boolean input = tag.getBoolean("input");
            return new ContainerPosition(pos, name, input);
        }

        public long getPos() {
            return pos().asLong();
        }
    }

    public static class Configuration {
        public boolean blacklist;
        public boolean compareTag;
        public Order order = Order.ROUND_ROBIN;
        private boolean dirty;

        public void read(CompoundTag tag) {
            if (tag.contains("Order")) {
                blacklist = tag.getBoolean("Blacklist");
                compareTag = tag.getBoolean("CompareTag");
                order = Order.valueOf(tag.getString("Order"));
            }
        }

        public void write(CompoundTag tag) {
            tag.putBoolean("Blacklist", blacklist);
            tag.putBoolean("CompareTag", compareTag);
            tag.putString("Order", order.name());
        }

        public void setDirty() {
            dirty = true;
        }

        public boolean isDirty() {
            return dirty;
        }

        public enum Order {
            FIRST,
            ROUND_ROBIN;

            public Order next() {
                return switch (this) {
                    case FIRST -> ROUND_ROBIN;
                    case ROUND_ROBIN -> FIRST;
                };
            }
        }

        public void encode(FriendlyByteBuf b) {
            b.writeBoolean(blacklist);
            b.writeBoolean(compareTag);
            b.writeEnum(order);
        }

        public void decode(FriendlyByteBuf b) {
            blacklist = b.readBoolean();
            compareTag = b.readBoolean();
            order = b.readEnum(Order.class);
        }
    }
}
