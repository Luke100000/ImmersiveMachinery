package immersive_machinery.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import immersive_machinery.Common;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Common.locate("immersive_machinery_tab"));

    public static final KeyMapping HORN;

    static {
        HORN = newKey("horn", GLFW.GLFW_KEY_H);
    }

    private static KeyMapping newKey(String name, int code) {
        KeyMapping key = new KeyMapping(
                "key.immersive_machinery." + name,
                InputConstants.Type.KEYSYM,
                code,
                CATEGORY
        );
        list.add(key);
        return key;
    }
}
