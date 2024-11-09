package immersive_machinery.client.gui;

import immersive_aircraft.client.gui.SlotRenderer;
import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_machinery.Common;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MachinerySlotRenderer implements SlotRenderer.Renderer {
    private static final ResourceLocation TEXTURE = Common.locate("textures/gui/container/inventory.png");
    final int u, v;
    final boolean small;

    public MachinerySlotRenderer(int u, int v, boolean small) {
        this.u = u;
        this.v = v;
        this.small = small;
    }

    public void render(VehicleScreen screen, @NotNull GuiGraphics context, SlotDescription slot, int mouseX, int mouseY, float delta) {
        int s = small ? 18 : 22;
        int o = small ? 1 : 3;
        int x = screen.getX() + slot.x() - o;
        int y = screen.getY() + slot.y() - o;
        if (screen.getMenu().getVehicle().getInventory().getItem(slot.index()).isEmpty()) {
            context.blit(TEXTURE, x, y, u, v, s, s, 128, 128);
        } else {
            if (small) {
                context.blit(TEXTURE, x, y, 110, 0, s, s, 128, 128);
            } else {
                context.blit(TEXTURE, x, y, 0, 0, s, s, 128, 128);
            }
        }
    }
}
