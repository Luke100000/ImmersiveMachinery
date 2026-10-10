package immersive_machinery;

import immersive_aircraft.config.JsonConfig;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.network.NetworkManager;
import immersive_machinery.entity.inventory.ShardSlotDescription;
import immersive_machinery.entity.inventory.TooltippedSlotDescription;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static immersive_aircraft.entity.inventory.VehicleInventoryDescription.registerSlotType;
import static immersive_aircraft.item.upgrade.VehicleStat.register;

public final class Common {
    public static final String MOD_ID = "immersive_machinery";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    // Define slot types
    public static final String SLOT_FILTER = registerSlotType("im_filter", TooltippedSlotDescription::new, TooltippedSlotDescription::new);
    public static final String SLOT_SHARDS = registerSlotType("im_shards", ShardSlotDescription::new, ShardSlotDescription::new);


    // Register vehicle properties
    public static final VehicleStat DRILLING_SPEED = register("drillingSpeed", true, 1.0f);

    public static void init() {
        try {
            Files.createDirectories(JsonConfig.getConfigFile(MOD_ID).getParentFile().toPath());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the machinery configuration directory", e);
        }

        // Register animation variables
        BBAnimationVariables.register("drill");
        BBAnimationVariables.register("drill_rx");
        BBAnimationVariables.register("drill_rz");
        BBAnimationVariables.register("grabber");
        BBAnimationVariables.register("engine_vibration_x");
        BBAnimationVariables.register("engine_vibration_y");
        BBAnimationVariables.register("engine_vibration_z");
    }

    public static Identifier locate(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

