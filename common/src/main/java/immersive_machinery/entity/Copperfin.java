package immersive_machinery.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_machinery.Items;
import immersive_machinery.Sounds;
import immersive_machinery.client.KeyBindings;
import immersive_machinery.network.c2s.SonarMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Copperfin extends MachineEntity {
    public float waterSurface = 62.875f;
    public float dripping = 0.0f;
    public float bubbling = 0.0f;
    public int ambientSoundTime = 0;
    private int sonarCooldown = 0;

    public Copperfin(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    public Item asItem() {
        return Items.COPPERFIN.get();
    }

    @Override
    protected void updateController() {
        super.updateController();

        // Sonar
        sonarCooldown--;
        if (level().isClientSide() && KeyBindings.HORN.consumeClick() && sonarCooldown < 0) {
            NetworkHandler.sendToServer(new SonarMessage());
            sonarCooldown = 60;
        }

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
    public boolean worksUnderWater() {
        return true;
    }

    @Override
    protected SoundEvent getEngineStartSound() {
        return super.getEngineStartSound();
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.SUBMARINE_ENGINE.get();
    }

    @Override
    protected float getGravity() {
        return isUnderWater() ? 0.0f : super.getGravity();
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.25f;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);

        this.playSound(Sounds.HATCH_CLOSE.get());
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        this.playSound(Sounds.HATCH_OPEN.get());
    }

    public float getUnderwaterFraction() {
        return (float) Math.min(1.0f, Math.max(0.0f, (waterSurface - getY()) / getBbHeight()));
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

        if (level().isClientSide) {
            tickClient();
        } else {
            tickServer();
        }
    }

    public void tickClient() {
        float underwaterFraction = getUnderwaterFraction();

        // Wet particles
        if (underwaterFraction >= dripping) {
            dripping = underwaterFraction;
        } else if (underwaterFraction < 0.6) {
            dripping = Math.max(0.0f, dripping - 0.005f);
            float r = random.nextFloat();
            spawnParticlesAround(ParticleTypes.FALLING_WATER, 10.0f * dripping, 1.625f * r + 1.625f * underwaterFraction * (1.0f - r));
        }

        // Submerge particles
        if (underwaterFraction <= bubbling) {
            bubbling = underwaterFraction;
        } else if (underwaterFraction > 0.8) {
            bubbling = Math.min(1.0f, bubbling + 0.01f);
            spawnParticlesAround(ParticleTypes.BUBBLE, 10.0f, random.nextFloat() * underwaterFraction);
        }

        // Bubbles
        float rawSpeed = (float) getCurrentSpeed();
        double speed = rawSpeed * 3.0 + 0.5;
        if (underwaterFraction > 0.6 && level().isClientSide() && pressingInterpolatedZ.getSmooth() > -0.01f && isUnderWater()) {
            for (int i = 0; i < (int) (speed * 2.5f + random.nextFloat()); i++) {
                Vector4f pos = transformPosition(getVehicleTransform(), (random.nextFloat() - 0.5f) * 0.5f, (random.nextFloat() - 0.5f) * 0.5f + getBbHeight() * 0.5f + 0.1f, -1.0f);
                Vector3f vec = transformVector(getVehicleNormalTransform(), 0.0f, 0.0f, -1.0f * (pressingInterpolatedZ.getSmooth() + 0.25f));
                level().addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, vec.x, 0.0, vec.z);
            }
        }

        // Yaw and roll
        setXRot((float) (pressingInterpolatedY.getSmooth() * -15.0f * speed));
        setZRot((float) (pressingInterpolatedX.getSmooth() * -20.0f * speed));

        // Speed particles
        if (underwaterFraction > 0.0 && underwaterFraction < 1.0 && rawSpeed > 0) {
            for (int i = 0; i < rawSpeed * 8.0; i++) {
                Vector3f pos = getParticlePosition(0.0f);
                level().addParticle(ParticleTypes.SPLASH, pos.x, waterSurface, pos.z, 0.0, 1.0, 0.0);
            }

            if (this.random.nextInt(10) == 0) {
                this.playSound(SoundEvents.GENERIC_SPLASH, 1.0f, 0.8f + 0.4f * this.random.nextFloat());
            }
        }
    }

    public void tickServer() {
        // Ambient sounds
        if (isVehicle()) {
            ambientSoundTime -= (getY() < lastY - 0.00001 ? 1 : 3);
            if (ambientSoundTime <= 0) {
                ambientSoundTime = 100 + random.nextInt(250);
                this.playSound(Sounds.SUBMARINE_AMBIENCE.get());
            }
        }
    }

    public Vector3f getParticlePosition(float y) {
        boolean front = random.nextBoolean();
        float x, z;
        float w = 0.875f;
        float l = 1.0f;
        if (front) {
            x = (random.nextFloat() - 0.5f) * w;
            float mz = pressingInterpolatedZ.getSmooth();
            if (Math.abs(mz) < 0.01f) {
                z = random.nextBoolean() ? -l : l;
            } else {
                z = mz > 0.0f ? -l : l;
            }
        } else {
            x = random.nextBoolean() ? -w : w;
            z = (random.nextFloat() - 0.5f) * l;
        }
        Vector4f vector4f = transformPosition(getVehicleTransform(), x, y, z);
        return new Vector3f(vector4f.x, vector4f.y, vector4f.z);
    }

    protected void spawnParticlesAround(ParticleOptions type, float amount, float height) {
        for (int i = 0; i < amount + random.nextFloat(); i++) {
            Vector3f pos = getParticlePosition(height);
            level().addParticle(type, pos.x, pos.y, pos.z, 0.0, 1.0, 0.0);
        }
    }

    public void sonar() {
        level().getEntities(this, new AABB(getOnPos(), getOnPos()).inflate(48)).forEach(e -> {
            if (e instanceof LivingEntity le && !e.isPassengerOfSameVehicle(this)) {
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30));
            }
        });
        this.playSound(Sounds.SONAR.get());
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
