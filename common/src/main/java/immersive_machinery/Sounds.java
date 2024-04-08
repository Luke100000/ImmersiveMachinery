package immersive_machinery;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import immersive_aircraft.cobalt.registration.Registration;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> HEAVY_ENGINE_START = register("engine_start");

    static void bootstrap() {
        // nop
    }

    static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = Common.locate(name);
        return Registration.register(BuiltInRegistries.SOUND_EVENT, id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
