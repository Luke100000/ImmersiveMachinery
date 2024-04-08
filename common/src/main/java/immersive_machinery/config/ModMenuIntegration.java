package immersive_machinery.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import immersive_aircraft.config.ConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // TODO
        // return parent -> ConfigScreen.getScreen(Config.getInstance());
        return parent -> ConfigScreen.getScreen(immersive_aircraft.config.Config.getInstance());
    }
}