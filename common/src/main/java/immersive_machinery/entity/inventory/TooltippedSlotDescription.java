package immersive_machinery.entity.inventory;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class TooltippedSlotDescription extends SlotDescription {
    public TooltippedSlotDescription(String type, int index, int x, int y, JsonObject json) {
        super(type, index, x, y, json);
    }

    public TooltippedSlotDescription(String type, FriendlyByteBuf byteBuf) {
        super(type, byteBuf);
    }

    @Override
    public Optional<List<Component>> getToolTip() {
        return Optional.of(List.of(Component.translatable("gui.immersive_machinery.slot." + type)));
    }
}
