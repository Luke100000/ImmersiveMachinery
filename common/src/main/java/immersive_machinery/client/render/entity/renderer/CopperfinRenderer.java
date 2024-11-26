package immersive_machinery.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_machinery.Common;
import immersive_machinery.config.Config;
import immersive_machinery.entity.Copperfin;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.joml.Math.fma;

public class CopperfinRenderer<T extends Copperfin> extends MachineryRenderer<T> {
    private static final ResourceLocation ID = Common.locate("copperfin");

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    private Matrix3f getVehicleNormalTransform(T entity, float yaw, float tickDelta) {
        Matrix3f transform = new Matrix3f();
        transform.rotate(Axis.YP.rotationDegrees(-yaw));
        transform.rotate(Axis.XP.rotationDegrees(entity.getViewXRot(tickDelta)));
        transform.rotate(Axis.ZP.rotationDegrees(entity.getRoll(tickDelta)));
        return transform;
    }

    public void renderWaterMask(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource source, int light) {
        Matrix3f transform = getVehicleNormalTransform(entity, yaw, tickDelta);

        float z = 0.04f;
        float waterHeight = (float) (entity.waterSurface + z - (entity.getY() * tickDelta + entity.yOld * (1.0 - tickDelta)));
        boolean firstPerson = Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;
        float iz = firstPerson ? z : -z;

        // The lower square of the cutout
        Vector3f b0 = new Vector3f(-0.75f + iz, 0.125f + iz, -0.25f + iz);
        Vector3f b1 = new Vector3f(0.75f - iz, 0.125f + iz, -0.25f + iz);
        Vector3f b2 = new Vector3f(0.75f - iz, 0.125f + iz, 0.9375f - iz);
        Vector3f b3 = new Vector3f(-0.75f + iz, 0.125f + iz, 0.9375f - iz);

        // Calculate the global y coordinate for each corner
        float h0 = fma(transform.m01(), b0.x, fma(transform.m11(), b0.y, transform.m21() * b0.z));
        float h1 = fma(transform.m01(), b1.x, fma(transform.m11(), b1.y, transform.m21() * b1.z));
        float h2 = fma(transform.m01(), b2.x, fma(transform.m11(), b2.y, transform.m21() * b2.z));
        float h3 = fma(transform.m01(), b3.x, fma(transform.m11(), b3.y, transform.m21() * b3.z));

        float ht0 = fma(transform.m01(), b0.x, fma(transform.m11(), b0.y + 1.0f, transform.m21() * b0.z));
        float ht1 = fma(transform.m01(), b1.x, fma(transform.m11(), b1.y + 1.0f, transform.m21() * b1.z));
        float ht2 = fma(transform.m01(), b2.x, fma(transform.m11(), b2.y + 1.0f, transform.m21() * b2.z));
        float ht3 = fma(transform.m01(), b3.x, fma(transform.m11(), b3.y + 1.0f, transform.m21() * b3.z));

        // The height of the water relative to the lower square
        float maxHeight = 1.5f - z;
        float f0 = Math.max(0.0f, Math.min(maxHeight, (waterHeight - h0) / (ht0 - h0)));
        float f1 = Math.max(0.0f, Math.min(maxHeight, (waterHeight - h1) / (ht1 - h1)));
        float f2 = Math.max(0.0f, Math.min(maxHeight, (waterHeight - h2) / (ht2 - h2)));
        float f3 = Math.max(0.0f, Math.min(maxHeight, (waterHeight - h3) / (ht3 - h3)));

        // Not visible
        if (f0 == 0.0f || f1 == 0.0f || f2 == 0.0f || f3 == 0.0f) {
            return;
        }

        // The upper square of the cutout
        Vector3f t0 = new Vector3f(0.0f, f0, 0.0f).add(b0);
        Vector3f t1 = new Vector3f(0.0f, f1, 0.0f).add(b1);
        Vector3f t2 = new Vector3f(0.0f, f2, 0.0f).add(b2);
        Vector3f t3 = new Vector3f(0.0f, f3, 0.0f).add(b3);

        Vector3f[][] faces = {
                {t0, t1, t2, t3},
                {t3, t2, t1, t0},
                {b3, b2, b1, b0},
                {b0, b1, t1, t0},
                {b1, b2, t2, t1},
                {b2, b3, t3, t2},
                {b3, b0, t0, t3}
        };

        Vector3f[] normals = {
                new Vector3f(0.0f, -1.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, -1.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(-1.0f, 0.0f, 0.0f)
        };

        // Tint the water texture
        int averageWaterColor = BiomeColors.getAverageWaterColor(entity.level(), entity.getOnPos());

        // Only render the water mask when partly underwater
        boolean renderTop = f0 < maxHeight || f1 < maxHeight || f2 < maxHeight || f3 < maxHeight;
        boolean cameraInFluid = Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE;

        for (int j = renderTop ? 0 : 2; j < ((firstPerson || !cameraInFluid) ? 7 : 2); ++j) {
            VertexConsumer buffer = j < 2 ? source.getBuffer(RenderType.waterMask()) : source.getBuffer(RenderType.entityTranslucent(Common.locate("textures/white.png")));
            for (int i = 0; i < 4; ++i) {
                Vector3f v = faces[j][i];
                Vector3f n = normals[i];
                Vector4f v2 = matrixStack.last().pose().transform(new Vector4f(v.x, v.y + (j == 0 ? -z * 2 : 0.0f), v.z, 1.0f));
                buffer.vertex(v2.x, v2.y, v2.z);
                buffer.color(averageWaterColor | 0x70000000);
                buffer.uv(0.5f, 0.5f);
                buffer.overlayCoords(OverlayTexture.NO_OVERLAY);
                buffer.uv2(light);
                buffer.normal(matrixStack.last().normal(), n.x, n.y, n.z);
                buffer.endVertex();
            }
        }
    }

    @Override
    public void renderLocal(T entity, float yaw, float tickDelta, PoseStack matrixStack, PoseStack.Pose peek, MultiBufferSource vertexConsumerProvider, int light) {
        super.renderLocal(entity, yaw, tickDelta, matrixStack, peek, vertexConsumerProvider, light);

        if (Config.getInstance().waterRenderingFixForCopperfin) {
            renderWaterMask(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
        }
    }

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