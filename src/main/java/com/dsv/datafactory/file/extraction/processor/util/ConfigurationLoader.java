package com.dsv.datafactory.file.extraction.processor.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigurationLoader {
    private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());

    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList("password",
            "secret", "token", "key", "pass"));

    private static boolean isSensitive(String key) {
        return SENSITIVE_KEYS.stream().anyMatch(key.toLowerCase()::contains);
    }

    public static String getOrDefault(String envName, String defaultValue) {
        return getOrDefault(envName, defaultValue, false);
    }

    public static String getOrDefault(String envName, String defaultValue, boolean isEnvValueSecretHidden) {
        String value = System.getenv(envName);

        if (value == null) {
            logger.info(() -> "Using default value for: " + envName);
            return defaultValue;
        }

        if (isEnvValueSecretHidden || isSensitive(envName)) {
            logger.info(() -> "Using hidden value for: " + envName);
            return value;
        }
        logger.info(() -> "Value found for " + envName);
        return value;
    }

    public static String getOrFail(String envName) {
        String value = System.getenv(envName);
        if (value == null) {
            throw new IllegalStateException("Missing required environment variable: " + envName);
        }
        return value;
    }
}
