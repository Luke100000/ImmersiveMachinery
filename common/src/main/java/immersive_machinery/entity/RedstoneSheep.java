package immersive_machinery.entity;

import immersive_machinery.Items;
import immersive_machinery.Sounds;
import immersive_machinery.Utils;
import immersive_machinery.config.Config;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RedstoneSheep extends NavigatingMachine {
    private static final int INVENTORY_BUFFER_SPACE = 3;
    private static final int RESCAN_INTERVAL = 100;

    private BlockPos home;
    private BlockPos task;
    private int reloadingTicks;
    private int rescanningTicks;
    private int tasksHarvested;

    // The set of blocks which require ac
    private Set<BlockPos> workingSet = new HashSet<>();
    private Set<BlockPos> backlogSet = new HashSet<>();

    public RedstoneSheep(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, false, false, 0);

        setMaxUpStep(1.1f);
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.REDSTONE_SHEEP.get();
    }

    @Override
    protected float getEnginePitch() {
        float speed = (float) getSpeedVector().length();
        return Math.min(1.0f, 0.75f + speed * 10.0f);
    }

    @Override
    protected float getEngineReactionSpeed() {
        return 10f;
    }

    @Override
    public void tick() {
        super.tick();

        // Update home
        if (home == null || home.getX() == 0 && home.getY() == 0 && home.getZ() == 0) {
            home = this.blockPosition();
        }

        // Rotate to target
        double dx = getX() - lastX;
        double dz = getZ() - lastZ;
        if (dx * dx + dz * dz > 0.00001) {
            setYRot(Utils.lerpAngle(getYRot(), (float) Math.toDegrees(Math.atan2(dz, dx)) + 90.0f, 10.0f));
        } else {
            setYRot(Utils.lerpAngle(getYRot(), (float) (Math.floor(getYRot() / 90.0f + 0.5f) * 90.0f), 5.0f));
        }

        if (level().isClientSide) {
            return;
        }

        // Time to return home
        if (isFuelLow() || isInventoryFull()) {
            reloadingTicks = 60;
        }

        rescanningTicks--;

        setEngineTarget(task != null ? 1.0f : 0.0f);

        // The sheep's state machine (loading/unloading -> working -> rescanning)
        if (reloadingTicks > 0) {
            // Head home, wait until loading and unloading is done
            if (moveTo(home) || !navigator.hasPath()) {
                reloadingTicks--;
            }
        } else if (task != null) {
            // Head to target and do work
            if (moveTo(task)) {
                VerifyState state = verify(task);
                if (state == VerifyState.VALID) {
                    work(task);
                }
                task = null;
            } else if (!navigator.hasPath()) {
                // Target is unreachable, remove from list
                backlogSet.remove(task);
                task = null;
            }
        } else if (!workingSet.isEmpty()) {
            // Pick the closest task, verify, set as task, and move to backlog
            if ((level().getGameTime() + getId()) % 5 == 0) {
                //noinspection OptionalGetWithoutIsPresent
                BlockPos closest = workingSet.stream().min(Comparator.comparingDouble(a -> a.distToCenterSqr(getX(), getY(), getZ()))).get();
                VerifyState state = verify(closest);
                if (state == VerifyState.VALID) {
                    task = closest;
                    backlogSet.add(closest);
                    tasksHarvested++;
                    rescanningTicks = RESCAN_INTERVAL;
                } else if (state == VerifyState.NOT_MATURE) {
                    backlogSet.add(closest);
                }
                workingSet.remove(closest);
            }
        } else if (!backlogSet.isEmpty()) {
            // Switch sets
            workingSet = backlogSet;
            backlogSet = new HashSet<>();

            // If last run was unproductive, rescan
            if (tasksHarvested == 0) {
                if (rescanningTicks <= 0) {
                    // If no players are nearby, why would the world change noticeable?
                    if (playerIsClose()) {
                        rescan();
                    }
                    rescanningTicks = RESCAN_INTERVAL;
                }

                // Return home and wait
                reloadingTicks = 60;
            }
            tasksHarvested = 0;
        } else if (rescanningTicks <= 0) {
            // Never found any tasks, rescan
            rescan();
            rescanningTicks = RESCAN_INTERVAL;
        }
    }

    @Override
    public boolean isVehicle() {
        return true;
    }

    private void rescan() {
        workingSet.clear();
        backlogSet.clear();

        int range = Config.getInstance().redstoneSheepMinHorizontalScanRange;

        // Breadth-first search with up to "range" of skip range
        LongOpenHashSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(home);
        visited.add(home.asLong());

        while (!queue.isEmpty()) {
            BlockPos origin = queue.poll();
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = new BlockPos(x + origin.getX(), origin.getY(), z + origin.getZ());
                    if (visited.contains(pos.asLong())) {
                        continue;
                    }
                    visited.add(pos.asLong());
                    if (verify(pos) != VerifyState.INVALID) {
                        workingSet.add(pos);
                        queue.add(pos);
                    }
                }
            }
        }
    }

    private boolean playerIsClose() {
        return level().getNearestPlayer(this, 32.0) != null;
    }

    private void work(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (level() instanceof ServerLevel serverLevel) {
            // Collect drops
            Block.getDrops(state, serverLevel, pos, null).forEach(stack -> {
                ItemStack remainder = addItem(stack);
                if (!remainder.isEmpty()) {
                    Block.popResource(serverLevel, pos, remainder);
                }
            });

            // Harvest or set age to 0 if possible
            getAgeProperty(state).ifPresentOrElse(
                    age -> serverLevel.setBlockAndUpdate(pos, state.setValue(age, 0)),
                    () -> serverLevel.destroyBlock(pos, false)
            );

            // Burn fuel
            consumeFuel(Config.getInstance().fuelTicksPerHarvest);

            // Spawn particles
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), pos.getX(), pos.getY(), pos.getZ(), 10, 0.5, 0.0, 0.5, 1.0);

            // Make sound
            serverLevel.playSound(null, pos, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.BLOCKS, 1.0f, 1.2f);
        }
    }

    enum VerifyState {
        VALID,
        NOT_MATURE,
        INVALID
    }

    /**
     * @param pos BlockPos to verify
     * @return The crop state of the block
     */
    private VerifyState verify(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        Block block = state.getBlock();

        if (isCrop(block)) {
            if (isMature(state)) {
                return VerifyState.VALID;
            } else {
                return VerifyState.NOT_MATURE;
            }
        } else {
            return VerifyState.INVALID;
        }
    }

    /**
     * @param block Block to check
     * @return Whether the block is harvestable crop
     */
    public static boolean isCrop(Block block) {
        String key = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (Config.getInstance().validCrops.containsKey(key)) {
            return Config.getInstance().validCrops.get(key);
        } else {
            return block instanceof CropBlock || block instanceof NetherWartBlock || block instanceof CocoaBlock || block instanceof PitcherCropBlock;
        }
    }

    public static Optional<Property<Integer>> getAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("age")) {
                try {
                    //noinspection unchecked
                    return Optional.of((Property<Integer>) property);
                } catch (ClassCastException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isMature(BlockState state) {
        return getAgeProperty(state)
                .filter(p -> !Objects.equals(state.getValue(p), Collections.max(p.getPossibleValues()))).isEmpty();
    }

    private boolean isInventoryFull() {
        return countItems() > getContainerSize() - INVENTORY_BUFFER_SPACE;
    }

    /**
     * @return Number of occupied slots in the inventory
     */
    private int countItems() {
        int i = 0;
        for (int j = 0; j < getContainerSize(); ++j) {
            ItemStack itemStack = getItem(j);
            if (!itemStack.isEmpty()) {
                i += 1;
            }
        }
        return i;
    }

    @Override
    public Item asItem() {
        return Items.REDSTONE_SHEEP.get();
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("HomeX", this.home.getX());
        tag.putInt("HomeY", this.home.getY());
        tag.putInt("HomeZ", this.home.getZ());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.home = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
    }

    @Override
    public void containerChanged(Container sender) {
        if (reloadingTicks > 0) {
            reloadingTicks = 60;
        }
    }

    @Override
    public float getFuelConsumption() {
        return 0.0f;
    }
}
