package immersive_machinery.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class BambooBeeConfigurationUpdate extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, BambooBeeConfigurationUpdate> STREAM_CODEC = StreamCodec.ofMember(BambooBeeConfigurationUpdate::encode, BambooBeeConfigurationUpdate::new);
    public static final CustomPacketPayload.Type<BambooBeeConfigurationUpdate> TYPE = Message.createType("bamboo_bee_configuration_update");

    private final int id;
    private final BambooBee.Configuration configuration;

    public BambooBeeConfigurationUpdate(BambooBee bee) {
        this.id = bee.getId();
        this.configuration = bee.getConfiguration();
    }

    public BambooBeeConfigurationUpdate(RegistryFriendlyByteBuf b) {
        this.id = b.readInt();
        configuration = new BambooBee.Configuration();
        configuration.decode(b);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(id);
        configuration.encode(b);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        read(e.level().getEntity(id));

        // Update other players
        for (Player player :  e.level().players()) {
            if (player != e && player instanceof ServerPlayer serverPlayer && player.distanceToSqr(e) < 256) {
                NetworkHandler.sendToPlayer(this, serverPlayer);
            }
        }
    }

    @Override
    public void receiveClient() {
        Common.networkManager.handleBambooBeeConfiguration(this);
    }

    public void read(Entity entity) {
        if (entity instanceof BambooBee bee) {
            bee.setConfiguration(configuration);
        }
    }

    public int getId() {
        return id;
    }
}
