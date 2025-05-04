package immersive_machinery.client.gui.screen;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.Common;
import immersive_machinery.client.KeyBindings;
import immersive_machinery.client.gui.screen.widgets.LegacyImageButton;
import immersive_machinery.entity.Copperfin;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CopperfinScreen extends VehicleScreen {
    private final Copperfin copperfin;

    private static final ResourceLocation TEXTURE = Common.locate("textures/gui/container/inventory.png");

    public CopperfinScreen(Copperfin copperfin, VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.copperfin = copperfin;
    }

    @Override
    protected void init() {
        super.init();

        // Sonar button
        MutableComponent text = Component.translatable("gui.immersive_machinery.copperfin.sonar", KeyBindings.HORN.getTranslatedKeyMessage());
        ImageButton help = new LegacyImageButton(getX() + 160, getY() + 5,
                10, 10,
                64, 0, 10, TEXTURE, 128, 128,
                b -> copperfin.requestSonar(), text);
        help.setTooltip(Tooltip.create(text));
        addRenderableWidget(help);
    }
}
