package immersive_machinery;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> TUNNEL_DIGGER = register("tunnel_digger");
    Supplier<SoundEvent> TUNNEL_DIGGER_DRILLING = register("tunnel_digger_drilling");
    Supplier<SoundEvent> HATCH_CLOSE = register("hatch_close");
    Supplier<SoundEvent> HATCH_OPEN = register("hatch_open");
    Supplier<SoundEvent> SONAR = register("sonar");
    Supplier<SoundEvent> SUBMARINE_AMBIENCE = register("submarine_ambience");
    Supplier<SoundEvent> SUBMARINE_ENGINE = register("submarine_engine");
    Supplier<SoundEvent> BAMBOO_BEE = register("bamboo_bee");
    Supplier<SoundEvent> REDSTONE_SHEEP = register("redstone_sheep");

    static void bootstrap() {
        // nop
    }

    static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = Common.locate(name);
        return Registration.register(BuiltInRegistries.SOUND_EVENT, id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
