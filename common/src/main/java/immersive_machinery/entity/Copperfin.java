package immersive_machinery.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Copperfin extends MachineEntity {
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

        // Yaw and roll
        setXRot(pressingInterpolatedY.getSmooth() * -20.0f);
        setZRot(pressingInterpolatedX.getSmooth() * -20.0f);
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
