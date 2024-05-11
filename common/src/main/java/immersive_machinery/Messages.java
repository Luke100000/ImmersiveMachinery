package immersive_machinery;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import immersive_machinery.network.c2s.TunnelDiggerControlsUpdate;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(TunnelDiggerControlsUpdate.class, TunnelDiggerControlsUpdate::new);
        NetworkHandler.registerMessage(BambooBeeConfigurationUpdate.class, BambooBeeConfigurationUpdate::new);
    }
}
