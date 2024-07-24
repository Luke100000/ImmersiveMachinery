package immersive_machinery.forge;

import immersive_machinery.Client;
import immersive_machinery.Common;
import immersive_machinery.Renderer;
import immersive_machinery.client.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Common.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class ClientForge {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        Renderer.bootstrap();
        Client.init();
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        KeyBindings.list.forEach(event::register);
    }
}
