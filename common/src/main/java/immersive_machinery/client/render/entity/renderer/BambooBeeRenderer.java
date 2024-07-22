package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

public class BambooBeeRenderer<T extends BambooBee> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("bamboo_bee");

    public static Path path;
    public static int pathEntity;

    public static void setPath(Path path, VehicleEntity vehicle) {
        BambooBeeRenderer.path = path;
        BambooBeeRenderer.pathEntity = vehicle.getId();
    }

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    public BambooBeeRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 0.5f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);

        if (pathEntity == entity.getId() && path != null && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
            renderPath(matrixStack, vertexConsumerProvider, path, entity.getX(), entity.getY(), entity.getZ());
        }
    }

    public static void renderPath(PoseStack poseStack, MultiBufferSource buffer, Path path, double x, double y, double z) {
        // Line
        renderPathLine(poseStack, buffer.getBuffer(RenderType.debugLineStrip(16.0)), path, x, y, z);

        BlockPos target = path.getTarget();
        if (distanceToCamera(target, x, y, z) <= 80.0f) {
            // Green target block
            renderCube(poseStack, buffer, x, y, z, target, 0.0f, 1.0f, 0.0f);

            // Blue and red path blocks
            for (int i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                float red = i == path.getNextNodeIndex() ? 1.0f : 0.0f;
                float blue = i == path.getNextNodeIndex() ? 0.0f : 1.0f;
                renderCube(poseStack, buffer, x, y, z, node.asBlockPos(), red, 0.0f, blue);
            }
        }
    }

    private static void renderCube(PoseStack poseStack, MultiBufferSource buffer, double x, double y, double z, BlockPos target, float red, float green, float blue) {
        AABB cube = new AABB((float) target.getX() + 0.25f, (float) target.getY() + 0.25f, (double) target.getZ() + 0.25, (float) target.getX() + 0.75f, (float) target.getY() + 0.75f, (float) target.getZ() + 0.75f).move(-x, -y, -z);
        DebugRenderer.renderFilledBox(poseStack, buffer, cube, red, green, blue, 0.5f);
    }

    public static void renderPathLine(PoseStack poseStack, VertexConsumer consumer, Path path, double x, double y, double z) {
        for (int i = 0; i < path.getNodeCount(); ++i) {
            Node node = path.getNode(i);
            if (distanceToCamera(node.asBlockPos(), x, y, z) > 80.0f) continue;
            float f = (float) i / (float) path.getNodeCount() * 0.33f;
            int j = i == 0 ? 0 : Mth.hsvToRgb(f, 0.9f, 0.9f);
            int k = j >> 16 & 0xFF;
            int l = j >> 8 & 0xFF;
            int m = j & 0xFF;
            consumer.vertex(poseStack.last().pose(), (float) ((double) node.x - x + 0.5f), (float) ((double) node.y - y + 0.5), (float) ((double) node.z - z + 0.5)).color(k, l, m, 255).endVertex();
        }
    }

    private static double distanceToCamera(BlockPos pos, double x, double y, double z) {
        return Math.abs((double) pos.getX() - x) + Math.abs((double) pos.getY() - y) + Math.abs((double) pos.getZ() - z);
    }
}

