package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.RedstoneSheep;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RedstoneSheepRenderer<T extends RedstoneSheep> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("redstone_sheep");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    public RedstoneSheepRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 0.5f;
    }

    @Override
    public void renderLocal(T entity, float yaw, float tickDelta, PoseStack matrixStack, PoseStack.Pose peek, MultiBufferSource vertexConsumerProvider, int light) {
        // Bobbing effect
        if (entity.getEnginePower() > 0.0) {
            matrixStack.translate(0.0f, (Math.cos((entity.tickCount + tickDelta) * 2.0f) + 1.0f) / 64.0f * entity.getEnginePower(), 0.0f);
        }

        super.renderLocal(entity, yaw, tickDelta, matrixStack, peek, vertexConsumerProvider, light);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PathDebugRenderer.INSTANCE.render(matrixStack, vertexConsumerProvider, entity, tickDelta);

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }
}
