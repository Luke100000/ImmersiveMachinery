package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

public class BambooBeeRenderer<T extends BambooBee> extends MachineryRenderer<T> {
    private static final Identifier ID = Common.locate("bamboo_bee");

    protected Identifier getModelId() {
        return ID;
    }

    private final ItemModelResolver itemModelResolver;

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    public BambooBeeRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();

        this.shadowRadius = 0.5f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(VehicleEntity entity) {
        return model;
    }

    @Override
    public VehicleRenderState<T> createRenderState() {
        return new BambooBeeRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, VehicleRenderState<T> state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        PathDebugRenderer.INSTANCE.render(entity, tickDelta);
        BambooBeeRenderState<T> beeState = (BambooBeeRenderState<T>) state;
        itemModelResolver.updateForNonLiving(beeState.carriedItem, entity.getInventory().getItem(BambooBee.WORK_SLOT), ItemDisplayContext.FIXED, entity);
    }

    @Override
    public void submit(VehicleRenderState<T> state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submit(state, matrixStack, collector, cameraState);

        matrixStack.pushPose();
        matrixStack.translate(0.0, -0.125, 0.0);
        ((BambooBeeRenderState<T>) state).carriedItem.submit(matrixStack, collector, state.light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrixStack.popPose();
    }

    private static class BambooBeeRenderState<T extends BambooBee> extends VehicleRenderState<T> {
        final ItemStackRenderState carriedItem = new ItemStackRenderState();
    }
}
