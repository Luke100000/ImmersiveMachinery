package immersive_machinery.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.Common;
import immersive_machinery.entity.BambooBee;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class BambooBeeConfigurationUpdate extends Message {
    private final int id;
    private final List<BambooBee.Configuration> configurations;

    public BambooBeeConfigurationUpdate(BambooBee bee) {
        this.id = bee.getId();
        this.configurations = bee.getConfigurations();
    }

    public BambooBeeConfigurationUpdate(FriendlyByteBuf b) {
        this.id = b.readInt();
        int size = b.readInt();
        configurations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BambooBee.Configuration configuration = new BambooBee.Configuration();
            configuration.decode(b);
            configurations.add(configuration);
        }
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeInt(id);
        b.writeInt(configurations.size());
        for (BambooBee.Configuration configuration : configurations) {
            configuration.encode(b);
        }
    }

    @Override
    public void receive(Player e) {
        if (e == null || e.level().isClientSide) {
            Common.networkManager.handleBambooBeeConfiguration(this);
        } else {
            read(e.level().getEntity(id));

            // Update other players
            for (Player player :  e.level().players()) {
                if (player != e && player instanceof ServerPlayer serverPlayer && player.distanceToSqr(e) < 256) {
                    NetworkHandler.sendToPlayer(this, serverPlayer);
                }
            }
        }
    }

    public void read(Entity entity) {
        if (entity instanceof BambooBee bee) {
            bee.getConfigurations().clear();
            bee.getConfigurations().addAll(configurations);
        }
    }

    public int getId() {
        return id;
    }
}
