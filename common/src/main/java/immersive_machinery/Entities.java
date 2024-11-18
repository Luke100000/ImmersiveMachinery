package immersive_machinery;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.entity.BambooBee;
import immersive_machinery.entity.Copperfin;
import immersive_machinery.entity.RedstoneSheep;
import immersive_machinery.entity.TunnelDigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public interface Entities {
    Supplier<EntityType<TunnelDigger>> TUNNEL_DIGGER = register("tunnel_digger", EntityType.Builder
            .of(TunnelDigger::new, MobCategory.MISC)
            .sized(2.8f, 2.8f)
            .clientTrackingRange(14)
            .fireImmune()
    );

    Supplier<EntityType<BambooBee>> BAMBOO_BEE = register("bamboo_bee", EntityType.Builder
            .of(BambooBee::new, MobCategory.MISC)
            .sized(0.8f, 0.625f)
            .clientTrackingRange(10)
            .fireImmune()
    );

    Supplier<EntityType<RedstoneSheep>> REDSTONE_SHEEP = register("redstone_sheep", EntityType.Builder
            .of(RedstoneSheep::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .clientTrackingRange(10)
            .fireImmune()
    );

    Supplier<EntityType<Copperfin>> COPPERFIN = register("copperfin", EntityType.Builder
            .of(Copperfin::new, MobCategory.MISC)
            .sized(1.5f, 1.625f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(Common.MOD_ID, name);
        return Registration.register(BuiltInRegistries.ENTITY_TYPE, id, () -> builder.build(id.toString()));
    }
}
