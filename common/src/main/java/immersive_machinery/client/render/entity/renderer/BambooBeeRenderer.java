package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BambooBeeRenderer<T extends BambooBee> extends MachineryRenderer<T> {
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

    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PathDebugRenderer.INSTANCE.render(matrixStack, vertexConsumerProvider, entity, tickDelta);

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);

        ItemStack stack = entity.getInventory().getItem(BambooBee.WORK_SLOT);
        if (!stack.isEmpty()) {
            matrixStack.translate(0.0, -0.125, 0.0);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, matrixStack, vertexConsumerProvider, entity.level(), entity.getId());
        }
    }
}

