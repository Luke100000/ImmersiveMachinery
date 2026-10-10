package immersive_machinery.network.s2c;

import immersive_machinery.Common;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class BambooBeeConfigurationSync extends BambooBeeConfigurationUpdate {
    public BambooBeeConfigurationSync(BambooBeeConfigurationUpdate message) {
        super(message);
    }

    public BambooBeeConfigurationSync(RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void receive(Player player) {
        Common.networkManager.handleBambooBeeConfiguration(this);
    }
}
