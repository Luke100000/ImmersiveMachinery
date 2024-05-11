package immersive_machinery.client.gui.screen;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class BambooBeeScreen extends VehicleScreen {
    private final BambooBee bee;

    private static final ResourceLocation TEXTURE = Common.locate("textures/gui/container/inventory.png");
    private static final Component TEXT_FILTER = Component.translatable("gui.immersive_machinery.bamboo_bee.filter");

    public BambooBeeScreen(BambooBee bee, VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.bee = bee;
    }

    protected void addImageButton(int x, int y, int u, int v, Button.OnPress onPress, Component button, Component tooltip, boolean active) {
        ImageButton inout = new ImageButton(getX() + x, getY() + y, 18, 18, u, v, 18, TEXTURE, 128, 128, onPress, button);
        inout.setTooltip(Tooltip.create(tooltip));
        inout.active = active;
        addRenderableWidget(inout);
    }

    @Override
    protected void init() {
        super.init();

        for (int i = 0; i < 3; ++i) {
            // Input/output
            addImageButton(40, 20 + i * 20, 18, 18, b -> {
                b.active = !b.active;
            }, TEXT_FILTER, TEXT_FILTER, true);

            // Whitelist/blacklist
            addImageButton(40 + 18, 20 + i * 20, 54, 18, b -> {
                b.active = !b.active;
            }, TEXT_FILTER, TEXT_FILTER, true);

            // Tag filter
            addImageButton(40 + 18, 20 + i * 20, 36, 18, b -> {
                b.active = !b.active;
            }, TEXT_FILTER, TEXT_FILTER, true);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    public void updateConfigurations() {
        init();
    }
}
