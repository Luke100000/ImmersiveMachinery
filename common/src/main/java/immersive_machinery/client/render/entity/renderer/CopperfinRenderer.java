package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.Copperfin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CopperfinRenderer<T extends Copperfin> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("copperfin");

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    protected ResourceLocation getModelId() {
        return ID;
    }

    public CopperfinRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 0.6f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }
}