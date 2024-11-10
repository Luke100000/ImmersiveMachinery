package immersive_machinery;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> TUNNEL_DIGGER = register("tunnel_digger");
    Supplier<SoundEvent> TUNNEL_DIGGER_DRILLING = register("tunnel_digger_drilling");

    static void bootstrap() {
        // nop
    }

    static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = Common.locate(name);
        return Registration.register(BuiltInRegistries.SOUND_EVENT, id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
