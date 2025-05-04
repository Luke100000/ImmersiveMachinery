package immersive_machinery.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_machinery.entity.TunnelDigger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class TunnelDiggerControlsUpdate extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, TunnelDiggerControlsUpdate> STREAM_CODEC = StreamCodec.ofMember(TunnelDiggerControlsUpdate::encode, TunnelDiggerControlsUpdate::new);
    public static final CustomPacketPayload.Type<TunnelDiggerControlsUpdate> TYPE = Message.createType("tunnel_digger_controls_update");

    private final int drillY;
    private final boolean drilling;

    public TunnelDiggerControlsUpdate(int drillY, boolean drilling) {
        this.drillY = drillY;
        this.drilling = drilling;
    }

    public TunnelDiggerControlsUpdate(RegistryFriendlyByteBuf b) {
        drillY = b.readInt();
        drilling = b.readBoolean();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(drillY);
        b.writeBoolean(drilling);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        if (e.getRootVehicle() instanceof TunnelDigger entity) {
            entity.drillY = drillY;
            entity.drilling = drilling;
        }
    }
}
