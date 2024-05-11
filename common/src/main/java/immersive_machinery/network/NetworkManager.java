package immersive_machinery.network;

import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;

public interface NetworkManager {
    void handleBambooBeeConfiguration(BambooBeeConfigurationUpdate bambooBeeConfigurationUpdate);
}
