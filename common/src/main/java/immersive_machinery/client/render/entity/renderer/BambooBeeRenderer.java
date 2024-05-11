package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BambooBeeRenderer <T extends BambooBee> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("bamboo_bee");

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
}

