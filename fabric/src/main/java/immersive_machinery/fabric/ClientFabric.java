package immersive_machinery.fabric;

import immersive_machinery.Renderer;
import immersive_machinery.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Renderer.bootstrap();

        KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
    }
}
