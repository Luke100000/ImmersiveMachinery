package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_machinery.Common;
import immersive_machinery.entity.RedstoneSheep;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RedstoneSheepRenderer<T extends RedstoneSheep> extends MachineryRenderer<T> {
    private static final Identifier ID = Common.locate("redstone_sheep");

    protected Identifier getModelId() {
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
    public void extractRenderState(T entity, VehicleRenderState<T> state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        PathDebugRenderer.INSTANCE.render(entity, tickDelta);
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(VehicleEntity entity) {
        return model;
    }
}
