package immersive_machinery.entity.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_machinery.client.render.entity.renderer.PathDebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.*;
import org.joml.Vector3d;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PilotNavigator {
    private final int followRange = 128;

    private final VehicleEntity vehicle;
    private final Mob pilot;
    private final PathFinder pathFinder;
    private final double speed;
    private final boolean isFlying;
    private final int accuracy;

    private BlockPos target;
    private Path currentPath;
    private int stuckTime;

    record Index(BlockPos pos1, BlockPos pos2) {
    }

    Cache<Object, Object> pathCache = CacheBuilder.newBuilder()
            .maximumSize(256)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public PilotNavigator(InventoryVehicleEntity vehicle, boolean isFlying, int accuracy) {
        this.vehicle = vehicle;
        this.pilot = new Bee(EntityType.BEE, vehicle.level());

        // Increase follow range
        AttributeInstance instance = pilot.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (instance != null) {
            instance.setBaseValue(followRange);
        }

        // Initialize pathfinder
        NodeEvaluator nodeEvaluator = isFlying ? new FlyNodeEvaluator() : new WalkNodeEvaluator();
        this.pathFinder = new PathFinder(nodeEvaluator, followRange * 16);
        this.speed = vehicle.getProperties().get(VehicleStat.ENGINE_SPEED);
        this.isFlying = isFlying;
        this.accuracy = accuracy;
    }

    public Vector3d getDirection() {
        BlockPos node = currentPath.isDone() ? currentPath.getTarget() : currentPath.getNextNodePos();
        double dx = node.getX() - vehicle.getX() + 0.5;
        double dy = isFlying ? node.getY() - (vehicle.getY() + vehicle.getBbHeight() / 2) + 0.5 : 0;
        double dz = node.getZ() - vehicle.getZ() + 0.5;
        return new Vector3d(dx, dy, dz);
    }

    public void moveTo(BlockPos pos) {
        if (!pos.equals(target) && !pos.equals(vehicle.blockPosition())) {
            target = pos;
            stuckTime = 0;
            currentPath = findPath(pos);

            // Do not accept incomplete paths
            if (currentPath != null && currentPath.getDistToTarget() > accuracy + 0.5) {
                currentPath = null;
            }

            // Debug
            PathDebugRenderer.INSTANCE.setPath(currentPath, vehicle);
        }
    }

    private Path findPath(BlockPos pos) {
        Index index = new Index(vehicle.blockPosition(), pos);

        // Fetch or create path
        Path path = (Path) pathCache.getIfPresent(index);
        if (path == null) {
            pilot.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());

            BlockPos blockPos = vehicle.blockPosition();
            int i = followRange + 8;
            PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(vehicle.level(), blockPos.offset(-i, -i, -i), blockPos.offset(i, i, i));
            path = pathFinder.findPath(pathNavigationRegion, pilot, Set.of(pos), followRange, accuracy, 1.0f);

            if (path == null) {
                return null;
            }
            pathCache.put(index, path);
        }

        // Clone path
        List<Node> nodes = new LinkedList<>();
        for (int i = 0; i < path.getNodeCount(); i++) {
            nodes.add(path.getNode(i));
        }

        return new Path(nodes, path.getTarget(), false);
    }

    public void tick() {
        if (currentPath != null) {
            followThePath();
            unstuck();
        } else {
            move(0, 0, 0);
        }
    }

    protected void followThePath() {
        Vector3d d = getDirection();
        double distance = d.length();
        double margin = 0.1;

        // Target reached
        if (distance < margin) {
            if (currentPath.isDone()) {
                currentPath = null;
                target = null;
            } else {
                currentPath.advance();
                stuckTime = 0;
            }
        }

        // Move
        double s = speed / (distance + 0.00001);
        s = Math.min(s, distance);
        move(d.x * s, d.y * s, d.z * s);
    }

    private void move(double x, double y, double z) {
        vehicle.setDeltaMovement(x, y, z);
    }

    private void unstuck() {
        stuckTime++;
        if (stuckTime >= 100 && currentPath != null) {
            currentPath = null;
            target = null;
        }
    }

    public boolean hasPath() {
        return currentPath != null;
    }

    public boolean isFlying() {
        return isFlying;
    }
}
