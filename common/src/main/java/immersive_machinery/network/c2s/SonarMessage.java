package immersive_machinery.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_machinery.entity.Copperfin;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class SonarMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, SonarMessage> STREAM_CODEC = StreamCodec.ofMember(SonarMessage::encode, SonarMessage::new);
    public static final CustomPacketPayload.Type<SonarMessage> TYPE = Message.createType("sonar");

    public SonarMessage() {
        super();
    }

    public SonarMessage(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void receiveServer(ServerPlayer player) {
        if (player.getRootVehicle() instanceof Copperfin copperfin) {
            copperfin.sonar();
        }
    }
}
