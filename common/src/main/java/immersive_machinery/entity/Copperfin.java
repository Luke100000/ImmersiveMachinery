package immersive_machinery.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Copperfin extends MachineEntity {
    public float waterSurface = 62.875f;

    public Copperfin(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    protected void updateController() {
        super.updateController();

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getSpeed() * getProperties().get(VehicleStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vector3f direction = getForwardDirection();

        // accelerate
        float thrust = getSpeed() * pressingInterpolatedZ.getSmooth();
        Vector3f f = direction.mul(thrust);
        setDeltaMovement(getDeltaMovement().add(f.x, f.y, f.z));
    }

    @Override
    protected float getSpeed() {
        return super.getSpeed() * (isUnderWater() ? 1.0f : 0.0f);
    }

    @Override
    public float getEnginePower() {
        return 1.0f; //TODO remove on update
    }

    @Override
    protected float getGravity() {
        return super.getGravity() * (isUnderWater() ? 1.0f - getEnginePower() : 1.0f);
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.25f;
    }

    @Override
    public void tick() {
        super.tick();

        // Scan for surface
        if (level().getGameTime() % 13 == 0) {
            boolean air = false;
            for (int y = (int) (getY() + getBbHeight()); y >= getBlockY(); y--) {
                FluidState fluidState = level().getFluidState(new BlockPos(getBlockX(), y, getBlockZ()));
                if (fluidState.isEmpty()) {
                    air = true;
                } else if (air) {
                    float ownHeight = fluidState.getOwnHeight();
                    waterSurface = y + ownHeight;
                    break;
                }
            }
        }

        // Reset air supply for passengers
        getPassengers().forEach(passenger -> {
            if (passenger instanceof LivingEntity livingEntity) {
                livingEntity.setAirSupply(livingEntity.getMaxAirSupply());
            }
        });

        // Bubbles
        if (level().isClientSide() && pressingInterpolatedZ.getSmooth() > 0.0f && isUnderWater()) {
            Vector4f pos = transformPosition(getVehicleTransform(), (random.nextFloat() - 0.5f) * 0.5f, (random.nextFloat() - 0.5f) * 0.5f + getBbHeight() * 0.5f, -1.25f);
            Vector3f vec = transformVector(getVehicleNormalTransform(), 0.0f, 0.0f, -1.0f * (pressingInterpolatedZ.getSmooth() + 0.25f));
            level().addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, vec.x, 0.0, vec.z);
        }

        // TODO: Dripping and half-submerged effects

        // Yaw and roll
        double speed = getCurrentSpeed() * 3.0 + 0.5;
        setXRot((float) (pressingInterpolatedY.getSmooth() * -15.0f * speed));
        setZRot((float) (pressingInterpolatedX.getSmooth() * -20.0f * speed));
    }

    protected double getCurrentSpeed() {
        double dx = getX() - xOld;
        double dz = getZ() - zOld;
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return this.getPassengers().size() < this.getPassengerSpace();
    }

    @Override
    public Vector3f getForwardDirection() {
        return new Vector3f(
                Mth.sin(-getYRot() * ((float) Math.PI / 180)),
                0.0f,
                Mth.cos(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    @Override
    public Vector3f getRightDirection() {
        return new Vector3f(
                Mth.cos(-getYRot() * ((float) Math.PI / 180)),
                0.0f,
                Mth.sin(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    public Float modifyWaterVision(float waterVision) {
        return waterVision * 0.25f + 0.75f;
    }
}
