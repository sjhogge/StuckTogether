package com.brooklynotter.stucktogether.configurations;

import com.brooklynotter.stucktogether.configurations.goodies.SphereConfigs;
import net.minecraftforge.fml.loading.FMLPaths;
import net.programmer.igoodie.goodies.configuration.ConfiGoodieOptions;
import net.programmer.igoodie.goodies.configuration.JsonConfiGoodie;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerConfigurations {

    public static final String DIR_PATH = FMLPaths.CONFIGDIR.get().toString() + "\\StuckTogether\\server";

    private static Map<String, JsonConfiGoodie> ALL_CONFIGS;
    public static SphereConfigs SPHERE;

    public static void initialize() {
        ALL_CONFIGS = new HashMap<>();
        SPHERE = readConfig("sphereConfig.json", new SphereConfigs());
    }

    public static void saveDirtyConfigs() {
        ALL_CONFIGS.forEach((fileName, config) -> {
            if (config.isDirty()) {
                config.saveToFile(getConfigFile(fileName));
            }
        });
    }

    /* ------------------- */

    public static File getConfigFile(String fileName) {
        return new File(DIR_PATH + "\\" + fileName);
    }

    private static <T extends JsonConfiGoodie> T readConfig(String fileName, T config) {
        ConfiGoodieOptions options = new ConfiGoodieOptions()
                .useFile(new File(DIR_PATH + "\\" + fileName))
                .moveInvalidConfigs((file, goodie) -> DIR_PATH + "\\" + fileName + ".invalid")
                .trimExcessiveFields();

        config.readConfig(options);

        ALL_CONFIGS.put(fileName, config);

        return config;
    }

}
