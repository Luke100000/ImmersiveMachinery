package immersive_machinery.config;

import immersive_aircraft.Main;
import immersive_aircraft.config.JsonConfig;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate(new Config(Main.MOD_ID), Config.class);

    public Config(String name) {
        super(name);
    }

    public static Config getInstance() {
        return INSTANCE;
    }


    @BooleanConfigEntry(true)
    public boolean allowHorn = true;
}
