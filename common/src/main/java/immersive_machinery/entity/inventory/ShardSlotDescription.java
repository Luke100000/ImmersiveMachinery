package immersive_machinery.entity.inventory;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.screen.slot.IngredientSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class ShardSlotDescription extends TooltippedSlotDescription {
    public ShardSlotDescription(String type, int index, int x, int y, JsonObject json) {
        super(type, index, x, y, json);
    }

    public ShardSlotDescription(String type, FriendlyByteBuf buffer) {
        super(type, buffer);
    }

    @Override
    public Slot getSlot(InventoryVehicleEntity vehicle, Container inventory) {
        // The ingredient is fixed on both sides, so only the base slot metadata needs syncing.
        return new IngredientSlot(Ingredient.of(Items.AMETHYST_SHARD), 64, inventory, index, x, y);
    }
}
