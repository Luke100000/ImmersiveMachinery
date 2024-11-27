package immersive_machinery.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_machinery.entity.Copperfin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class SonarMessage extends Message {
    public SonarMessage() {
        super();
    }

    public SonarMessage(FriendlyByteBuf buf) {

    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void receive(Player player) {
        if (player.getRootVehicle() instanceof Copperfin copperfin) {
            copperfin.sonar();
        }
    }
}
