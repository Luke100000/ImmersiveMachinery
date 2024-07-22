package immersive_machinery.entity.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_machinery.client.render.entity.renderer.BambooBeeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PilotNavigator {
    private final VehicleEntity vehicle;
    private final Mob pilot;
    private final PathFinder pathFinder;
    private BlockPos target;

    private Path currentPath;
    private int stuckTime;
    private final double speed;

    private final int followRange = 128;

    record Index(BlockPos pos1, BlockPos pos2) {
    }

    Cache<Object, Object> pathCache = CacheBuilder.newBuilder()
            .maximumSize(256)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public PilotNavigator(InventoryVehicleEntity vehicle) {
        this.vehicle = vehicle;
        this.pilot = new Bee(EntityType.BEE, vehicle.level());

        // Increase follow range
        AttributeInstance instance = pilot.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (instance != null) {
            instance.setBaseValue(followRange);
        }

        // Initialize pathfinder
        FlyNodeEvaluator nodeEvaluator = new FlyNodeEvaluator();
        this.pathFinder = new PathFinder(nodeEvaluator, followRange * 16);
        this.speed = vehicle.getProperties().get(VehicleStat.ENGINE_SPEED);
    }

    public void moveTo(BlockPos pos) {
        if (!pos.equals(target)) {
            target = pos;
            stuckTime = 0;
            currentPath = findPath(pos);

            // Debug
            BambooBeeRenderer.setPath(currentPath, vehicle);
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
            path = pathFinder.findPath(pathNavigationRegion, pilot, Set.of(pos), followRange, 1, 1.0f);

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
        if (!isDone()) {
            followThePath();
            unstuck();
        }
    }

    private boolean isDone() {
        return currentPath == null || currentPath.isDone();
    }

    protected void followThePath() {
        Node node = currentPath.getNextNode();
        double dx = node.x - vehicle.getX() + 0.5;
        double dy = node.y - vehicle.getY() + 0.5 - vehicle.getBbHeight() * 0.5;
        double dz = node.z - vehicle.getZ() + 0.5;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        boolean lastNode = currentPath.getNextNodeIndex() + 1 == currentPath.getNodeCount();
        double margin = lastNode ? 0.5 : (1.0 - vehicle.getBbWidth()) * 0.475;

        if (distance < margin * margin) {
            currentPath.advance();
            stuckTime = 0;
        } else if (!lastNode) {
            // Skip to the next node if
            Node nextNode = currentPath.getNode(currentPath.getNextNodeIndex() + 1);
            double ndx = nextNode.x - node.x;
            double ndy = nextNode.y - node.y;
            double ndz = nextNode.z - node.z;
            double nextSegmentDistance = Math.sqrt(ndx * ndx + ndy * ndy + ndz * ndz);
            if (distance < nextSegmentDistance) {
                currentPath.advance();
                stuckTime = 0;
            }
        }

        // Move
        double s = speed / distance;
        s = Math.min(s, distance);
        vehicle.setDeltaMovement(dx * s, dy * s, dz * s);
    }

    private void unstuck() {
        stuckTime++;
        if (stuckTime >= 100 && currentPath != null) {
            currentPath = null;
            target = null;
        }
    }
}
