package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.TunnelDiggerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TunnelDiggerEntityRenderer<T extends TunnelDiggerEntity> extends MachineryEntityRenderer<T> {
    private static final ResourceLocation ID = Common.locate("tunnel_digger");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    public TunnelDiggerEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 1.5f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }
}
