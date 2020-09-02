package utilities;

import smartcity.config.StaticConfig;

public class ConditionalExecutor {
    public static void debug(Runnable action) {
        if (StaticConfig.DEBUG) {
            action.run();
        }
    }
}
