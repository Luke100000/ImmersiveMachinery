package immersive_machinery;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.entity.TunnelDiggerEntity;
import immersive_machinery.item.MachineryItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> TUNNEL_DIGGER = register("tunnel_digger", () -> new MachineryItem(vehicleProp(),
            world -> new TunnelDiggerEntity(Entities.TUNNEL_DIGGER.get(), world)));

    static void bootstrap() {
    }

    static Item.Properties vehicleProp() {
        return new Item.Properties().stacksTo(1);
    }

    static List<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultInstance()).toList();
    }

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Supplier<Item> register = Registration.register(BuiltInRegistries.ITEM, Main.locate(name), item);
        items.add(register);
        return register;
    }
}
