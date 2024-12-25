package immersive_machinery.entity;

import immersive_machinery.entity.misc.PilotNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class NavigatingMachine extends MachineEntity {
    public final PilotNavigator navigator;

    public NavigatingMachine(EntityType<? extends MachineEntity> entityType, Level world, boolean canExplodeOnCrash, boolean isFlying, int pathAccuracy) {
        super(entityType, world, canExplodeOnCrash);

        this.navigator = new PilotNavigator(this, isFlying, pathAccuracy);
    }

    @Override
    public void tick() {
        super.tick();

        // Update navigator and movement controller
        if (!level().isClientSide) {
            this.navigator.tick();
        }
    }

    public boolean moveTowards(BlockPos pos) {
        return moveTo(pos, 0.6 + getBbWidth() / 2.0f);
    }

    public boolean moveTo(BlockPos pos) {
        return moveTo(pos, 0.5);
    }

    public boolean moveTo(BlockPos pos, double distance) {
        navigator.moveTo(pos);
        Vec3 center = pos.getCenter();
        return Math.max(
                Math.max(
                        Math.abs(center.x() - getX()),
                        Math.abs(center.z() - getZ())
                ),
                this.navigator.isFlying() ? Math.abs(center.y() - (getY() + getBbHeight() / 2) + 0.5) : 0.0
        ) < distance;
    }
}
