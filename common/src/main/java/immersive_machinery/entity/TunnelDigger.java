package immersive_machinery.entity;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.Common;
import immersive_machinery.Items;
import immersive_machinery.Sounds;
import immersive_machinery.Utils;
import immersive_machinery.client.KeyBindings;
import immersive_machinery.config.Config;
import immersive_machinery.network.c2s.TunnelDiggerControlsUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.LinkedList;
import java.util.List;

import static immersive_machinery.Utils.doubleToPos;

public class TunnelDigger extends MachineEntity {
    public int drillY = 0;
    public boolean drilling = false;
    public boolean drillOn = true;
    public boolean currentlyDrilling = false;

    public float drillingAnimation = 0;
    public float lastDrillingAnimation = 0;

    public float drillPower = 0.0f;

    public TunnelDigger(EntityType<? extends TunnelDigger> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    public Item asItem() {
        return Items.TUNNEL_DIGGER.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (!isVehicle()) {
            drillY = 0;
            drilling = false;
        } else if (drilling) {
            drillPower = Math.min(drillPower + getDrillSpeed() / 20.0f, 1.0f);

            if (drillPower > 0 && level() instanceof ServerLevel level) {
                // Check if vertical drilling is requested
                boolean diagonal = (Math.floorMod(Math.round(getYRot() / 45), 2) == 1) && Config.getInstance().allowDiagonalDrilling;

                // Collect all possible positions
                List<BlockPos> positions = new LinkedList<>();

                // Drilling
                float front = diagonal ? 2.0f : 1.75f;
                float height = 1.5f;
                Vector3f forwardDirection = getForwardDirection();
                Vector3f rightDirection = getRightDirection();
                Vector3f upDirection = getTopDirection();
                Vector3f center = new Vector3f(forwardDirection).mul(front).add(0, height, 0);
                center = center.add((float) getX(), (float) getY(), (float) getZ());
                for (int x = -(diagonal ? 2 : 1); x <= (diagonal ? 2 : 1); x++) {
                    for (int z = -Math.max(0, drillY) - Math.abs(drillY) * 2; z <= 1 - Math.max(0, drillY); z++) {
                        for (int y = -1 + drillY; y <= 1 + drillY; y++) {
                            float fz = z + 0.22f * Math.floorMod(x + y, 2);
                            double px = center.x + rightDirection.x * x + upDirection.x * y + forwardDirection.x * fz;
                            double py = center.y + rightDirection.y * x + upDirection.y * y + forwardDirection.y * fz;
                            double pz = center.z + rightDirection.z * x + upDirection.z * y + forwardDirection.z * fz;
                            positions.add(doubleToPos(px, py, pz));
                        }
                    }
                }

                currentlyDrilling = !positions.isEmpty();

                // Mine blocks
                while (!positions.isEmpty() && drillPower > 0) {
                    BlockPos blockPos = positions.remove(random.nextInt(positions.size()));
                    float destroySpeed = Utils.mineBlock(level, blockPos, this);
                    burnShards(destroySpeed);
                    drillPower -= destroySpeed;
                }
            }
        } else {
            currentlyDrilling = false;
        }

        // Update drilling animation
        lastDrillingAnimation = drillingAnimation;
        drillingAnimation += (drilling ? 1 : 0) * 0.1f;

        setMaxUpStep(drillY > 0 ? 1.1f : 0.55f);

        // Exhaust particles
        double chance = engineSpinUpStrength + getEnginePower();
        if (level().random.nextDouble() < chance) {
            boolean fire = level().random.nextFloat() < engineSpinUpStrength;
            Matrix4f transform = getVehicleTransform();
            Vector4f pos = transformPosition(transform, 1.0f, 2.7f, -1.4375f);
            level().addParticle(fire ? ParticleTypes.SMALL_FLAME : ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0, 0.1, 0.0);
        }
    }

    public float getDrillSpeed() {
        return (hasShards() ? 10.0f : 5.0f) * this.getProperties().get(Common.DRILLING_SPEED);
    }

    public boolean hasShards() {
        return getSlots(Common.SLOT_SHARDS).stream().anyMatch(slot -> !slot.isEmpty());
    }

    public void burnShards(float destroySpeed) {
        if (random.nextFloat() < destroySpeed / 64.0) {
            List<ItemStack> shards = getSlots(Common.SLOT_SHARDS);
            if (!shards.isEmpty()) {
                shards.get(random.nextInt(shards.size())).shrink(1);
            }
        }
    }

    @Override
    protected void updateController() {
        super.updateController();

        // Update drill controls
        int newDrillY = movementY > 0 ? 1 : movementY < 0 ? -1 : 0;
        boolean newDilling = (movementY != 0 || movementZ > 0) && drillOn && getEnginePower() > 0.1f;
        if (newDrillY != drillY || newDilling != drilling) {
            drillY = newDrillY;
            drilling = newDilling;
            NetworkHandler.sendToServer(new TunnelDiggerControlsUpdate(drillY, drilling));
        }

        // Lock into increments
        if (movementX == 0) {
            float yRot = getYRot();
            double step = Config.getInstance().allowDiagonalDrilling ? 45 : 90;
            double targetRotation = Math.round(yRot / step) * step;
            double speed = 0.06f;
            setYRot((float) (yRot * (1.0 - speed) + targetRotation * speed));

            // Lock onto full blocks
            if (movementZ == 0) {
                double vSpeed = 0.04f;
                Vec3 deltaMovement = getDeltaMovement();
                double dx = getX() - (Math.round(getX() - 0.5) + 0.5);
                double dz = getZ() - (Math.round(getZ() - 0.5) + 0.5);
                setDeltaMovement(deltaMovement.add(-dx * vSpeed, 0, -dz * vSpeed));
            }
        }

        // Turn off drill
        if (KeyBindings.HORN.consumeClick()) {
            drillOn = !drillOn;
            LivingEntity pilot = getControllingPassenger();
            if (pilot != null) {
                pilot.sendSystemMessage(Component.translatable(drillOn ? "immersive_machinery.tunnel_digger.drill_on" : "immersive_machinery.tunnel_digger.drill_off"));
            }
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vector3f p = getForwardDirection().negate().mul(2.0f).add((float) getX(), (float) getY(), (float) getZ());
        Vec3 position = new Vec3(p.x, p.y, p.z);
        for (Pose entityPose : passenger.getDismountPoses()) {
            if (DismountHelper.canDismountTo(level(), position, passenger, entityPose)) {
                passenger.setPose(entityPose);
                return position;
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    protected float getSpeed() {
        // TODO: currentlyDrilling needs to be a syed flag
        return currentlyDrilling ? 0.0f : super.getSpeed();
    }

    public boolean isTrackMoving() {
        return getSpeed() * pressingInterpolatedZ.getSmooth() > 0.001f;
    }

    @Override
    public void setAnimationVariables(float tickDelta) {
        super.setAnimationVariables(tickDelta);

        BBAnimationVariables.set("drill", getDrillingAnimation(tickDelta) * 64.0f);
        BBAnimationVariables.set("drill_rx", 0);
        BBAnimationVariables.set("drill_rz", 0);

        double p = enginePower.getSmooth();
        BBAnimationVariables.set("engine_vibration_x", (float) ((random.nextDouble() - 0.5) * p));
        BBAnimationVariables.set("engine_vibration_y", (float) ((random.nextDouble() - 0.5) * p));
        BBAnimationVariables.set("engine_vibration_z", (float) ((random.nextDouble() - 0.5) * p));
    }

    public float getDrillingAnimation(float tickDelta) {
        return lastDrillingAnimation + (drillingAnimation - lastDrillingAnimation) * tickDelta;
    }

    @Override
    public double getZoom() {
        return 4.0;
    }

    @Override
    protected SoundEvent getEngineSound() {
        return drilling ? Sounds.TUNNEL_DIGGER_DRILLING.get() : Sounds.TUNNEL_DIGGER.get();
    }
}
