package immersive_machinery.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class Copperfin extends MachineEntity {
    public Copperfin(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    protected void updateController() {
        super.updateController();

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getEnginePower() * getProperties().get(VehicleStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vector3f direction = getForwardDirection();

        // accelerate
        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * getProperties().get(VehicleStat.ENGINE_SPEED)) * pressingInterpolatedZ.getSmooth();
        Vector3f f = direction.mul(thrust);
        setDeltaMovement(getDeltaMovement().add(f.x, f.y, f.z));
    }

    @Override
    public void tick() {
        super.tick();
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
}
