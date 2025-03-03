package com.dsv.datafactory.file.extraction.processor.logging;

import com.dsv.logger.ECSLogger;
import lombok.Setter;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;

// Elevate constant to application.prooperties to some resources 5389 and aifactory
// Add @Setter from Lombok
// Use some style lint - to control style in code - even built into IDEA
@Setter
public class ECSLoggerProvider {
    public static final String LOG_APP_NAME_SDD_ENV_VAR = "LOG_APP_NAME_SDD";
    public static final String LOG_APP_ID_SDD_ENV_VAR = "LOG_APP_ID_SDD";
    public static final String LOG_LEVEL_ENV_VAR = "LOG_LEVEL";
    private static String appName = getOrDefault(LOG_APP_NAME_SDD_ENV_VAR, "aifactory");
    private static String appId = getOrDefault(LOG_APP_ID_SDD_ENV_VAR, "5389");
    private static ECSLogger.Level logLevel = ECSLogger.Level.valueOf(getOrDefault(LOG_LEVEL_ENV_VAR, "WARN"));

    private ECSLoggerProvider() {
    }

    public static ECSLogger getLogger(String className) {
        return getLogger(className, logLevel);
    }

    public static ECSLogger getLogger(String className, ECSLogger.Level level) {
        ECSLogger logger = new ECSLogger(appName, appId, className);
        logger.setLevel(className, level);
        return logger;
    }
}
