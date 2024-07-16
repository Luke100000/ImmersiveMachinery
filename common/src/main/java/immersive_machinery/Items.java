package immersive_machinery;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.entity.RedstoneSheep;
import immersive_machinery.entity.TunnelDigger;
import immersive_machinery.item.BambooBeeItem;
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
            world -> new TunnelDigger(Entities.TUNNEL_DIGGER.get(), world)));

    Supplier<Item> BAMBOO_BEE = register("bamboo_bee", () -> new BambooBeeItem(vehicleProp(),
            world -> new BambooBee(Entities.BAMBOO_BEE.get(), world)));

    Supplier<Item> REDSTONE_SHEEP = register("redstone_sheep", () -> new MachineryItem(vehicleProp(),
            world -> new RedstoneSheep(Entities.REDSTONE_SHEEP.get(), world)));

    Supplier<Item> REDSTONE_MECHANISM = register("redstone_mechanism", () -> new Item(new Item.Properties().stacksTo(64)));
    Supplier<Item> DIAMOND_DRILL = register("diamond_drill", () -> new Item(new Item.Properties().stacksTo(8)));

    static void bootstrap() {
    }

    static Item.Properties vehicleProp() {
        return new Item.Properties().stacksTo(1);
    }

    static List<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultInstance()).toList();
    }

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Supplier<Item> register = Registration.register(BuiltInRegistries.ITEM, Common.locate(name), item);
        items.add(register);
        return register;
    }
}
