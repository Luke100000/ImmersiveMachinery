package immersive_machinery;

import immersive_aircraft.client.gui.SlotRenderer;
import immersive_aircraft.client.gui.VehicleScreenRegistry;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.client.gui.MachinerySlotRenderer;
import immersive_machinery.client.gui.screen.BambooBeeScreen;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.network.ClientNetworkManager;
import net.minecraft.client.Minecraft;

public class Client {
    public static void init() {
        Common.networkManager = new ClientNetworkManager(Minecraft.getInstance());
    }

    static {
        // Register screen handlers
        VehicleScreenRegistry.register(BambooBee.class, (vehicle, player, message) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null && vehicle instanceof BambooBee bee) {
                VehicleScreenHandler handler = (VehicleScreenHandler) vehicle.createMenu(message.getSyncId(), client.player.getInventory(), client.player);
                assert handler != null;
                BambooBeeScreen screen = new BambooBeeScreen(bee, handler, client.player.getInventory(), vehicle.getDisplayName());
                client.player.containerMenu = screen.getMenu();
                client.setScreen(screen);
            }
        });

        // Register slot renderers
        SlotRenderer.register(Common.SLOT_SHARDS, new MachinerySlotRenderer(0, 22, false));
        SlotRenderer.register(Common.SLOT_FILTER, new MachinerySlotRenderer(110, 18, true));
    }
}
