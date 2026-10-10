package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PathDebugRenderer {
    public static final PathDebugRenderer INSTANCE = new PathDebugRenderer();

    private Path path;
    private int pathEntity;

    public void setPath(Path path, VehicleEntity vehicle) {
        this.path = path;
        this.pathEntity = vehicle.getId();
    }

    public void render(Entity entity, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (entity == null || pathEntity != entity.getId() || path == null || !client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) return;

        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());

        try (Gizmos.TemporaryCollection ignored = client.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            BlockPos target = path.getTarget();
            if (distanceToCamera(target, x, y, z) <= 80.0) {
                renderCube(target, 0x8000FF00);
            }

            for (int i = 0; i < path.getNodeCount(); i++) {
                Node node = path.getNode(i);
                if (distanceToCamera(node.asBlockPos(), x, y, z) > 80.0) continue;

                renderCube(node.asBlockPos(), i == path.getNextNodeIndex() ? 0x80FF0000 : 0x800000FF);
                if (i > 0) {
                    Node previous = path.getNode(i - 1);
                    int color = Mth.hsvToRgb((float) i / path.getNodeCount() * 0.33f, 0.9f, 0.9f) | 0xFF000000;
                    Gizmos.line(new Vec3(previous.x + 0.5, previous.y + 0.5, previous.z + 0.5),
                            new Vec3(node.x + 0.5, node.y + 0.5, node.z + 0.5), color, 16.0f);
                }
            }
        }
    }

    private void renderCube(BlockPos pos, int color) {
        Gizmos.cuboid(new AABB(pos).deflate(0.25), GizmoStyle.fill(color));
    }

    private double distanceToCamera(BlockPos pos, double x, double y, double z) {
        return Math.abs(pos.getX() - x) + Math.abs(pos.getY() - y) + Math.abs(pos.getZ() - z);
    }
}
