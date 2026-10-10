package immersive_machinery;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import immersive_machinery.network.s2c.BambooBeeConfigurationSync;
import immersive_machinery.network.c2s.SonarMessage;
import immersive_machinery.network.c2s.TunnelDiggerControlsUpdate;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerServerbound("machinery_tunnel_digger_controls", TunnelDiggerControlsUpdate.class, TunnelDiggerControlsUpdate::new);
        NetworkHandler.registerServerbound("machinery_bamboo_bee_configuration", BambooBeeConfigurationUpdate.class, BambooBeeConfigurationUpdate::new);
        NetworkHandler.registerClientbound("machinery_bamboo_bee_configuration_sync", BambooBeeConfigurationSync.class, BambooBeeConfigurationSync::new);
        NetworkHandler.registerServerbound("machinery_sonar", SonarMessage.class, SonarMessage::new);
    }
}
