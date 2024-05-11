package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.RedstoneSheep;
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
    protected ModelPartRenderHandler<T> getModel(T entity) {
        return model;
    }
}
