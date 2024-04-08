package immersive_machinery.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    public static final KeyMapping horn;

    static {
        horn = newKey("horn", GLFW.GLFW_KEY_H);
    }

    private static KeyMapping newKey(String name, int code) {
        KeyMapping key = new KeyMapping(
                "key.immersive_machinery." + name,
                InputConstants.Type.KEYSYM,
                code,
                "itemGroup.immersive_machinery.immersive_machinery_tab"
        );
        list.add(key);
        return key;
    }
}
