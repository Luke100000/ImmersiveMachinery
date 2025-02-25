package immersive_machinery;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import immersive_machinery.network.c2s.SonarMessage;
import immersive_machinery.network.c2s.TunnelDiggerControlsUpdate;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(Common.SHORT_MOD_ID, TunnelDiggerControlsUpdate.class, TunnelDiggerControlsUpdate::new);
        NetworkHandler.registerMessage(Common.SHORT_MOD_ID, BambooBeeConfigurationUpdate.class, BambooBeeConfigurationUpdate::new);
        NetworkHandler.registerMessage(Common.SHORT_MOD_ID, SonarMessage.class, SonarMessage::new);
    }
}
