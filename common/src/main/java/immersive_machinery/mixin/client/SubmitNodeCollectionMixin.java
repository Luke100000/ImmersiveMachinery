package immersive_machinery.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin {
    @Shadow
    @Final
    public SimpleFeatureRenderPhase waterMask;

    @Inject(method = "submitCustomGeometry", at = @At("HEAD"), cancellable = true)
    private void immersive_machinery$submitWaterMask(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer, CallbackInfo ci) {
        if (renderType != RenderTypes.waterMask()) return;

        // Depth-only masks must run after translucent geometry, like vanilla boat masks.
        waterMask.submit(new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType, renderer));

        ci.cancel();
    }
}
