package immersive_machinery.client.gui.screen;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.Common;
import immersive_machinery.client.KeyBindings;
import immersive_machinery.client.gui.screen.widgets.LegacyImageButton;
import immersive_machinery.entity.TunnelDigger;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TunnelDiggerScreen extends VehicleScreen {
    private final TunnelDigger digger;

    private static final ResourceLocation TEXTURE = Common.locate("textures/gui/container/inventory.png");

    public TunnelDiggerScreen(TunnelDigger digger, VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.digger = digger;
    }

    @Override
    protected void init() {
        super.init();

        // Drill button
        MutableComponent text = Component.translatable("gui.immersive_machinery.tunnel_digger.drill", KeyBindings.HORN.getTranslatedKeyMessage());
        ImageButton help = new LegacyImageButton(getX() + 160, getY() + 5,
                10, 10,
                64, 0, 10, TEXTURE, 128, 128,
                b -> digger.toggleDrill(), text);
        help.setTooltip(Tooltip.create(text));
        addRenderableWidget(help);
    }
}
