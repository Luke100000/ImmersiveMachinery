package immersive_machinery;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_machinery.network.c2s.BambooBeeConfigurationUpdate;
import immersive_machinery.network.c2s.SonarMessage;
import immersive_machinery.network.c2s.TunnelDiggerControlsUpdate;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(Common.MOD_ID, TunnelDiggerControlsUpdate.TYPE, TunnelDiggerControlsUpdate.STREAM_CODEC);
        NetworkHandler.registerMessage(Common.MOD_ID, BambooBeeConfigurationUpdate.TYPE, BambooBeeConfigurationUpdate.STREAM_CODEC);
        NetworkHandler.registerMessage(Common.MOD_ID, SonarMessage.TYPE, SonarMessage.STREAM_CODEC);
    }
}
