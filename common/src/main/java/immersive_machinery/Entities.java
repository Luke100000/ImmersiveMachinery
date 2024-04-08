package immersive_machinery;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.registration.Registration;
import immersive_machinery.entity.TunnelDiggerEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public interface Entities {
    Supplier<EntityType<TunnelDiggerEntity>> TUNNEL_DIGGER = register("tunnel_digger", EntityType.Builder
            .of(TunnelDiggerEntity::new, MobCategory.MISC)
            .sized(1.3f, 0.6f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(Main.MOD_ID, name);
        return Registration.register(BuiltInRegistries.ENTITY_TYPE, id, () -> builder.build(id.toString()));
    }
}
