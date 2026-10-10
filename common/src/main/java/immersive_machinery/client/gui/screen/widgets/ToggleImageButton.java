package immersive_machinery.client.gui.screen.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ToggleImageButton extends LegacyImageButton {
    private boolean pressed;

    public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier resourceLocation, int textureWidth, int textureHeight, OnPress onPress, Component message) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress, message);
    }

    @Override
    public void renderTexture(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int uOffset, int vOffset, int textureDifference, int width, int height, int textureWidth, int textureHeight) {
        int i = vOffset;
        if (!isActive() || isPressed()) {
            i += textureDifference * 2;
        } else if (isHovered()) {
            i += textureDifference;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, uOffset, i, width, height, textureWidth, textureHeight);
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        pressed = !pressed;
        super.onPress(input);
    }
}
