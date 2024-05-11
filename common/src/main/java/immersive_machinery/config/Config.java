package immersive_machinery.config;

import immersive_aircraft.config.JsonConfig;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_machinery.Common;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate(new Config(Common.MOD_ID), Config.class);

    public Config(String name) {
        super(name);
    }

    public static Config getInstance() {
        return INSTANCE;
    }


    @BooleanConfigEntry(true)
    public boolean allowHorn;

    @BooleanConfigEntry(false)
    public boolean allowDiagonalDrilling;

    // TODO map for boosting
}
