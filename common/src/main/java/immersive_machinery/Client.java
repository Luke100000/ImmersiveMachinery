package immersive_machinery;

import immersive_aircraft.client.gui.SlotRenderer;
import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.client.gui.VehicleScreenRegistry;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.client.gui.MachinerySlotRenderer;
import immersive_machinery.client.gui.screen.BambooBeeScreen;
import immersive_machinery.client.gui.screen.CopperfinScreen;
import immersive_machinery.client.gui.screen.TunnelDiggerScreen;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.entity.Copperfin;
import immersive_machinery.entity.TunnelDigger;
import immersive_machinery.network.ClientNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class Client {
    public static void init() {
        Common.networkManager = new ClientNetworkManager(Minecraft.getInstance());
    }

    static {
        // Register screen handlers
        registerVehicleScreen(BambooBee.class, (vehicle, handler, player) -> new BambooBeeScreen(vehicle, handler, player.getInventory(), vehicle.getDisplayName()));
        registerVehicleScreen(TunnelDigger.class, (vehicle, handler, player) -> new TunnelDiggerScreen(vehicle, handler, player.getInventory(), vehicle.getDisplayName()));
        registerVehicleScreen(Copperfin.class, (vehicle, handler, player) -> new CopperfinScreen(vehicle, handler, player.getInventory(), vehicle.getDisplayName()));

        // Register slot renderers
        SlotRenderer.register(Common.SLOT_SHARDS, new MachinerySlotRenderer(0, 22, false));
        SlotRenderer.register(Common.SLOT_FILTER, new MachinerySlotRenderer(110, 18, true));
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    private static <T extends VehicleEntity, S extends VehicleScreen> void registerVehicleScreen(Class<T> vehicleClass, TriFunction<T, VehicleScreenHandler, LocalPlayer, S> screenFactory) {
        VehicleScreenRegistry.register(vehicleClass, (vehicle, player, message) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null && vehicleClass.isInstance(vehicle)) {
                T castedVehicle = vehicleClass.cast(vehicle);
                VehicleScreenHandler handler = (VehicleScreenHandler) vehicle.createMenu(message.getSyncId(), client.player.getInventory(), client.player);
                assert handler != null;
                S screen = screenFactory.apply(castedVehicle, handler, client.player);
                client.player.containerMenu = screen.getMenu();
                client.setScreen(screen);
            }
        });
    }
}
