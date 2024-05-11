package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.TunnelDigger;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TunnelDiggerRenderer<T extends TunnelDigger> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("tunnel_digger");

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<T>()
            .add(
                    "belt_anim",
                    (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> {
                        String engine = "frame_" + (entity.enginePower.getSmooth() > 0.01 ? entity.tickCount % 2 : 0);
                        renderOptionalObject(engine, model, vertexConsumerProvider, entity, matrixStack, light, time);
                    }
            )
            .add(
                    "right_track_anim",
                    (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> {
                        String engine = "right_track_anim_" + (entity.isTrackMoving() ? entity.tickCount % 4 : 0);
                        renderOptionalObject(engine, model, vertexConsumerProvider, entity, matrixStack, light, time);
                    }
            )
            .add(
                    "left_track_anim",
                    (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> {
                        String engine = "left_track_anim_" + (entity.isTrackMoving() ? entity.tickCount % 4 : 0);
                        renderOptionalObject(engine, model, vertexConsumerProvider, entity, matrixStack, light, time);
                    }
            );

    protected ResourceLocation getModelId() {
        return ID;
    }

    public TunnelDiggerRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 1.5f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }
}
