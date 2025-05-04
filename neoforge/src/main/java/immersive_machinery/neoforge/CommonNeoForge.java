package immersive_machinery.neoforge;

import immersive_machinery.*;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

@net.neoforged.fml.common.Mod(Common.MOD_ID)
public final class CommonNeoForge {
    public CommonNeoForge(IEventBus bus) {
        Common.init();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();

        Messages.loadMessages();

        DEF_REG.register(bus);
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(CREATIVE_MODE_TAB, Common.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = DEF_REG.register(Common.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );
}
