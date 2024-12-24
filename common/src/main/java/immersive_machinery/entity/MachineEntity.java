package immersive_machinery.entity;

import immersive_aircraft.entity.EngineVehicle;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public abstract class MachineEntity extends EngineVehicle {
    public MachineEntity(EntityType<? extends MachineEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround()) {
            setZRot(roll * 0.9f);
        } else {
            float speed = (float) getDeltaMovement().length();
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(VehicleStat.ROLL_FACTOR) * speed);
        }

        super.tick();
    }

    @Override
    protected void updateController() {
        // Boot up the engine
        setEngineTarget(1.0f);

        // Left and right
        setYRot(getYRot() - getProperties().get(VehicleStat.YAW_SPEED) * pressingInterpolatedX.getSmooth());

        if (onGround()) {
            // get direction
            Vector3f direction = getForwardDirection();

            // speeds
            float speed = getSpeed() * pressingInterpolatedZ.getSmooth();

            // accelerate
            setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(speed))));
        }
    }

    protected float getSpeed() {
        return (float) (Math.pow(getEnginePower(), 2.0) * getProperties().get(VehicleStat.ENGINE_SPEED));
    }

    @Override
    protected void updateVelocity() {
        float decay = 1.0f - getProperties().get(VehicleStat.FRICTION);
        float gravity = getGravity();
        if (wasTouchingWater) {
            gravity *= 0.25f;
            decay = 0.9f;
        } else if (onGround()) {
            decay = getProperties().get(VehicleStat.GROUND_FRICTION);
        }

        float hd = getProperties().get(VehicleStat.HORIZONTAL_DECAY);
        float vd = getProperties().get(VehicleStat.VERTICAL_DECAY);
        Vec3 velocity = getDeltaMovement();
        setDeltaMovement(velocity.x * decay * hd, velocity.y * decay * vd + gravity, velocity.z * decay * hd);
        float rf = decay * getProperties().get(VehicleStat.ROTATION_DECAY);
        pressingInterpolatedX.decay(0.0f, 1.0f - rf);
        pressingInterpolatedZ.decay(0.0f, 1.0f - rf);
    }

    // Inventory utils

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemStack);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemStack);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (ItemStack.isSameItemSameTags(itemStack, stack)) {
                this.moveItemsBetweenStacks(stack, itemStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack stack, ItemStack other) {
        int i = Math.min(this.getMaxStackSize(), other.getMaxStackSize());
        int j = Math.min(stack.getCount(), i - other.getCount());
        if (j > 0) {
            other.grow(j);
            stack.shrink(j);
            this.setChanged();
        }
    }

    private void moveItemToEmptySlots(ItemStack stack) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            if (canPlaceItem(i, stack)) {
                ItemStack itemStack = this.getItem(i);
                if (itemStack.isEmpty()) {
                    this.setItem(i, stack.copyAndClear());
                    return;
                }
            }
        }
    }
}

