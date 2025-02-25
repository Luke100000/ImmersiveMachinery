package immersive_machinery;

import immersive_aircraft.entity.inventory.slots.IngredientSlotDescription;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.network.NetworkManager;
import immersive_machinery.entity.inventory.TooltippedSlotDescription;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static immersive_aircraft.entity.inventory.VehicleInventoryDescription.registerSlotType;
import static immersive_aircraft.item.upgrade.VehicleStat.register;

public final class Common {
    public static final String MOD_ID = "immersive_machinery";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    // Define slot types
    public static final String SLOT_FILTER = registerSlotType("im_filter", TooltippedSlotDescription::new, TooltippedSlotDescription::new);
    public static final String SLOT_SHARDS = registerSlotType("im_shards",
            (type, index, x, y, json) -> new IngredientSlotDescription(type, index, x, y, json, Ingredient.of(Items.AMETHYST_SHARD), 64),
            IngredientSlotDescription::new);


    // Register vehicle properties
    public static final VehicleStat DRILLING_SPEED = register("drillingSpeed", true, 1.0f);

    public static void init() {
        // Register animation variables
        BBAnimationVariables.register("drill");
        BBAnimationVariables.register("drill_rx");
        BBAnimationVariables.register("drill_rz");
        BBAnimationVariables.register("grabber");
        BBAnimationVariables.register("engine_vibration_x");
        BBAnimationVariables.register("engine_vibration_y");
        BBAnimationVariables.register("engine_vibration_z");
    }

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

