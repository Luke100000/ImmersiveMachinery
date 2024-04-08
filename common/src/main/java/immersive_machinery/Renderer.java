package immersive_machinery;


import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.client.render.entity.renderer.TunnelDiggerEntityRenderer;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.TUNNEL_DIGGER.get(), TunnelDiggerEntityRenderer::new);
    }
}