package immersive_machinery.client;

import immersive_machinery.entity.MachineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class OverlayRenderer {
    static final OverlayRenderer INSTANCE = new OverlayRenderer();

    public static void renderOverlay(GuiGraphics context, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (!client.options.hideGui && client.gameMode != null && client.player != null && client.player.getRootVehicle() instanceof MachineEntity machinery) {
            INSTANCE.renderMachineryGui(client, context, tickDelta, machinery);
        }
    }

    private void renderMachineryGui(Minecraft client, GuiGraphics context, float tickDelta, MachineEntity machinery) {
        assert client.level != null;
    }
}
