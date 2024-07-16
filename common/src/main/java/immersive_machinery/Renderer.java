package immersive_machinery;


import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.client.render.entity.renderer.BambooBeeRenderer;
import immersive_machinery.client.render.entity.renderer.TunnelDiggerRenderer;
import immersive_machinery.client.render.entity.renderer.RedstoneSheepRenderer;
import immersive_machinery.client.render.entity.renderer.CopperfinRenderer;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.TUNNEL_DIGGER.get(), TunnelDiggerRenderer::new);
        Registration.register(Entities.BAMBOO_BEE.get(), BambooBeeRenderer::new);
        Registration.register(Entities.REDSTONE_SHEEP.get(), RedstoneSheepRenderer::new);
        Registration.register(Entities.COPPERFIN.get(), CopperfinRenderer::new);
    }
}