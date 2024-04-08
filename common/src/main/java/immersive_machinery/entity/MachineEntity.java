package immersive_machinery.entity;

import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class MachineEntity extends EngineVehicle {
    public MachineEntity(EntityType<? extends MachineEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void updateController() {

    }

    @Override
    protected void updateVelocity() {

    }
}

