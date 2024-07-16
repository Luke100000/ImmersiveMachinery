package immersive_machinery.client.gui.screen;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_machinery.Common;
import immersive_machinery.client.gui.screen.widgets.ToggleImageButton;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Locale;

public class BambooBeeScreen extends VehicleScreen {
    private final BambooBee bee;

    private static final ResourceLocation TEXTURE = Common.locate("textures/gui/container/inventory.png");

    private static final Component TEXT_HELP = Component.translatable("gui.immersive_machinery.bamboo_bee.help");
    private static final Component TEXT_WHITELIST = Component.translatable("gui.immersive_machinery.bamboo_bee.whitelist");
    private static final Component TEXT_BLACKLIST = Component.translatable("gui.immersive_machinery.bamboo_bee.blacklist");
    private static final Component TEXT_TAG = Component.translatable("gui.immersive_machinery.bamboo_bee.tag");

    public BambooBeeScreen(BambooBee bee, VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.bee = bee;
    }

    protected void addImageButton(int x, int y, int u, int v, Button.OnPress onPress, Component text, boolean pressed) {
        ToggleImageButton b = new ToggleImageButton(getX() + x, getY() + y, 16, 16, u, v, 16, TEXTURE, 128, 128, onPress, text);
        b.setTooltip(Tooltip.create(text));
        b.setPressed(pressed);
        addRenderableWidget(b);
    }

    @Override
    public void onClose() {
        super.onClose();

        BambooBee.Configuration c = bee.getConfiguration();
        if (c.isDirty()) {
            NetworkHandler.sendToServer(new BambooBeeConfigurationUpdate(bee));
        }
    }

    @Override
    protected void init() {
        super.init();

        clearWidgets();

        BambooBee.Configuration c = bee.getConfiguration();

        // Whitelist/blacklist
        addImageButton(50, 20, 48, 80, b -> {
            c.blacklist = !c.blacklist;
            c.setDirty();
            init();
        }, c.blacklist ? TEXT_BLACKLIST : TEXT_WHITELIST, c.blacklist);

        // Tag filter
        addImageButton(50, 20 + 18, 32, 80, b -> {
            c.compareTag = !c.compareTag;
            c.setDirty();
        }, TEXT_TAG, c.compareTag);

        // Round-robin
        addImageButton(50, 20 + 36, 0, 80, b -> {
            c.order = c.order.next();
            c.setDirty();
            init();
        }, Component.translatable("gui.immersive_machinery.bamboo_bee.order." + c.order.name().toLowerCase(Locale.ROOT)), false);

        // Help button
        ImageButton help = new ImageButton(getX() + 161, getY() + 4,
                10, 10,
                64, 0, 10, TEXTURE, 128, 128,
                b -> openHelp(), TEXT_HELP);
        help.setTooltip(Tooltip.create(Component.translatable("gui.immersive_machinery.bamboo_bee.help")));
        addRenderableWidget(help);
    }

    private void openHelp() {
        try {
            Util.getPlatform().openUri(URI.create("https://github.com/Luke100000/ImmersiveMachinery/wiki/bamboo-bee"));
        } catch (Exception e) {
            Common.LOGGER.error("Failed to open help page", e);
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
