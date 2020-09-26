package utilities;

import smartcity.config.StaticConfig;

public class ConditionalExecutor {
    public static void debug(Runnable action) {
        if (StaticConfig.DEBUG || StaticConfig.TRACE) {
            action.run();
        }
    }

    public static void trace(Runnable action) {
        if (StaticConfig.TRACE) {
            action.run();
        }
    }
}
