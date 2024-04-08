package immersive_machinery;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Common {
    public static final String SHORT_MOD_ID = "ic_ma";
    public static final String MOD_ID = "immersive_machinery";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        // nop
    }

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
