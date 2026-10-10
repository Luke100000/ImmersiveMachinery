package immersive_machinery.client;

import immersive_machinery.entity.MachineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class OverlayRenderer {
    static final OverlayRenderer INSTANCE = new OverlayRenderer();

    public static void renderOverlay(GuiGraphicsExtractor context, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (!client.gui.hud.isHidden() && client.gameMode != null && client.player != null && client.player.getRootVehicle() instanceof MachineEntity machinery) {
            INSTANCE.renderMachineryGui(client, context, tickDelta, machinery);
        }
    }

    private void renderMachineryGui(Minecraft client, GuiGraphicsExtractor context, float tickDelta, MachineEntity machinery) {
        assert client.level != null;
    }
}
