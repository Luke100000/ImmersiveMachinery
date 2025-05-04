package immersive_machinery.entity;

import com.mojang.serialization.Codec;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.Common;
import immersive_machinery.Items;
import immersive_machinery.Sounds;
import immersive_machinery.Utils;
import immersive_machinery.entity.inventory.ContainerPosition;
import immersive_machinery.item.BambooBeeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BambooBee extends NavigatingMachine {
    private Configuration configuration = new Configuration();
    private final List<ContainerPosition> containerPositions = new LinkedList<>();
    private Task currentTask;
    private int searchCooldown = 0;

    public static final int WORK_SLOT = 0;

    public BambooBee(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, false, true, 1);
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.BAMBOO_BEE.get();
    }

    @Override
    protected float getEnginePitch() {
        float speed = (float) getSpeedVector().length();
        return 0.65f + speed * 1.25f;
    }

    @Override
    protected double getDefaultGravity() {
        return (1.0f - getEnginePower()) * super.getDefaultGravity();
    }

    @Override
    public void tick() {
        super.tick();

        // Rotate to direction
        if (level().isClientSide) {
            double dx = x - secondLastX;
            double dy = y - secondLastY;
            double dz = z - secondLastZ;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > 0.0f && d2 < 10.0f) {
                float yRot = getYRot();
                setXRot(Utils.lerpAngle(getXRot(), (float) (dy * 45.0f), 2.0f));
                setYRot(Utils.lerpAngle(yRot, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, 8.0f));
                setZRot(Utils.lerpAngle(getRoll(), (getYRot() - yRot) * 2.0f, 2.0f));

                // Smoke particles
                if (level().getGameTime() % 4 == 0) {
                    level().addParticle(ParticleTypes.SMOKE, x, y + 0.4, z, 0.0, 0.0, 0.0);
                }
            } else {
                setXRot(Utils.lerpAngle(getXRot(), 0.0f, 5.0f));
                setZRot(Utils.lerpAngle(getRoll(), 0.0f, 5.0f));
            }

            return;
        }

        setEngineTarget(currentTask != null ? 1.0f : 0.0f);

        if (currentTask == null) {
            // Find the task
            searchCooldown--;
            if (searchCooldown <= 0) {
                currentTask = getTask();
                if (currentTask == null) {
                    searchCooldown = 60;
                }
            }
        } else {
            ItemStack carries = getSlot(WORK_SLOT).get();
            if (carries.isEmpty()) {
                // Fetch item
                if (moveTowards(currentTask.source())) {
                    BlockEntity blockEntity = level().getBlockEntity(currentTask.source());
                    if (blockEntity instanceof Container container) {
                        ItemStack item = container.getItem(currentTask.slot());
                        if (match(item, currentTask.stack())) {
                            ItemStack stack = container.removeItem(currentTask.slot(), item.getCount());
                            getSlot(WORK_SLOT).set(stack);
                            playSound(SoundEvents.BARREL_OPEN);
                            blockEntity.setChanged();
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
                if (moveTowards(currentTask.target())) {
                    BlockEntity blockEntity = level().getBlockEntity(currentTask.target());
                    if (blockEntity instanceof Container container) {
                        addToContainer(container, carries);
                        playSound(SoundEvents.BARREL_CLOSE);
                        blockEntity.setChanged();
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

    @Override
    public boolean isVehicle() {
        return true;
    }

    @Override
    protected void handlePortal() {
        // Do nothing
    }

    private void addToContainer(Container container, ItemStack carries) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (container.canPlaceItem(slot, carries)) {
                if (ItemStack.isSameItemSameComponents(carries, stack)) {
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

    private List<ContainerPosition> getPositions(boolean input) {
        List<ContainerPosition> positions = new LinkedList<>();
        for (ContainerPosition position : containerPositions) {
            if (position.input() == input) {
                positions.add(position);
            }
        }

        // Round-robin
        if (configuration.order == Configuration.Order.ROUND_ROBIN) {
            Collections.rotate(positions, (int) level().getGameTime());
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
                if (ItemStack.isSameItemSameComponents(item, stack)) {
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
            return ItemStack.isSameItemSameComponents(stack, other);
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
    public void addItemTag(ItemStack stack) {
        super.addItemTag(stack);

        stack.set(BambooBeeItem.CONTAINER_POSITIONS, containerPositions);
        stack.set(BambooBeeItem.CONFIGURATION, configuration);
    }

    @Override
    public void readItemTag(ItemStack stack) {
        super.readItemTag(stack);

        containerPositions.clear();
        containerPositions.addAll(stack.getOrDefault(BambooBeeItem.CONTAINER_POSITIONS, new LinkedList<>()));

        configuration = stack.getOrDefault(BambooBeeItem.CONFIGURATION, new Configuration());
    }

    private void readConfiguration(CompoundTag tag) {
        // Read container positions
        if (tag.contains("ContainerPositions")) {
            ListTag list = tag.getList("ContainerPositions", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag containerTag = list.getCompound(i);
                containerPositions.add(new ContainerPosition(containerTag));
            }
        }

        if (tag.contains("Configuration")) {
            CompoundTag configTag = tag.getCompound("Configuration");
            configuration = new Configuration(configTag);
        } else {
            configuration = new Configuration();
        }
    }

    private void writeConfiguration(CompoundTag tag) {
        // Save container positions
        ListTag list = new ListTag();
        for (ContainerPosition position : containerPositions) {
            list.add(position.toTag());
        }
        tag.put("ContainerPositions", list);
        tag.put("Configuration", configuration.toTag());
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

    public static class Configuration {
        public static Codec<Configuration> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.AS_CODEC)
                .xmap(Configuration::new, Configuration::toTag);

        public boolean blacklist;
        public boolean compareTag;
        public Order order = Order.ROUND_ROBIN;
        private boolean dirty;

        public Configuration() {

        }

        public Configuration(CompoundTag tag) {
            if (tag.contains("Order")) {
                blacklist = tag.getBoolean("Blacklist");
                compareTag = tag.getBoolean("CompareTag");
                order = Order.valueOf(tag.getString("Order"));
            }
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Blacklist", blacklist);
            tag.putBoolean("CompareTag", compareTag);
            tag.putString("Order", order.name());
            return tag;
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
