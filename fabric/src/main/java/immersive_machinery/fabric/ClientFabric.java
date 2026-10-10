package immersive_machinery.fabric;

import immersive_machinery.Client;
import immersive_machinery.Renderer;
import immersive_machinery.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Renderer.bootstrap();
        Client.init();

        KeyBindings.list.forEach(KeyMappingHelper::registerKeyMapping);
    }
}
