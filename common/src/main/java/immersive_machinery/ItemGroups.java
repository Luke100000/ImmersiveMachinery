package immersive_machinery;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ItemGroups {
    public static Identifier getIdentifier() {
        return Common.locate(Common.MOD_ID + "_tab");
    }

    public static Component getDisplayName() {
        return Component.translatable("itemGroup." + ItemGroups.getIdentifier().toLanguageKey());
    }

    public static ItemStack getIcon() {
        return Items.TUNNEL_DIGGER.get().getDefaultInstance();
    }
}
