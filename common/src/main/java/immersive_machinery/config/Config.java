package immersive_machinery.config;

import immersive_aircraft.config.JsonConfig;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;
import immersive_machinery.Common;

import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate(new Config(Common.MOD_ID), Config.class);

    public Config(String name) {
        super(name);
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    @BooleanConfigEntry(true)
    public boolean waterRenderingFixForCopperfin;

    @IntegerConfigEntry(8)
    public int redstoneSheepMinHorizontalScanRange;

    @IntegerConfigEntry(20)
    public int fuelTicksPerHarvest;

    public Map<String, Boolean> validCrops = Map.of(
            "minecraft:grass", true
    );
}
