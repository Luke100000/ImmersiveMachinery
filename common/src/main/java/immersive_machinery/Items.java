package immersive_machinery;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.entity.Copperfin;
import immersive_machinery.entity.RedstoneSheep;
import immersive_machinery.entity.TunnelDigger;
import immersive_machinery.item.BambooBeeItem;
import immersive_machinery.item.MachineryItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> TUNNEL_DIGGER = register("tunnel_digger", name -> new MachineryItem(vehicleProp(name),
            world -> new TunnelDigger(Entities.TUNNEL_DIGGER.get(), world)));

    Supplier<Item> BAMBOO_BEE = register("bamboo_bee", name -> new BambooBeeItem(vehicleProp(name),
            world -> new BambooBee(Entities.BAMBOO_BEE.get(), world)));

    Supplier<Item> REDSTONE_SHEEP = register("redstone_sheep", name -> new MachineryItem(vehicleProp(name),
            world -> new RedstoneSheep(Entities.REDSTONE_SHEEP.get(), world)));

    Supplier<Item> COPPERFIN = register("copperfin", name -> new MachineryItem(vehicleProp(name),
            world -> new Copperfin(Entities.COPPERFIN.get(), world)));

    Supplier<Item> REDSTONE_MECHANISM = register("redstone_mechanism", name -> new Item(baseProps(name).stacksTo(8)));
    Supplier<Item> IRON_DRILL = register("iron_drill", name -> new Item(baseProps(name).stacksTo(8)));
    Supplier<Item> DIAMOND_DRILL = register("diamond_drill", name -> new Item(baseProps(name).stacksTo(8)));
    Supplier<Item> NETHERITE_DRILL = register("netherite_drill", name -> new Item(baseProps(name).stacksTo(8)));

    static void bootstrap() {
    }

    static Item.Properties vehicleProp(String name) {
        return baseProps(name).stacksTo(1);
    }

    static Item.Properties baseProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Common.locate(name)));
    }

    static List<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultInstance()).toList();
    }

    static Supplier<Item> register(String name, Function<String, Item> item) {
        Supplier<Item> register = Registration.register(BuiltInRegistries.ITEM, Common.locate(name), () -> item.apply(name));
        items.add(register);
        return register;
    }
}
