package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static smartcity.config.StaticConfig.DEBUG;
import static smartcity.config.StaticConfig.TRACE;

public class ConditionalExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ConditionalExecutor.class);

    public static void debug(Runnable action) {
        debug(action, false);
    }

    public static void debug(Runnable action, boolean necessaryCondition) {
        if ((DEBUG || TRACE) && necessaryCondition) {
            try {
                action.run();
            } catch (Exception e) {
                logger.info("Error running debug action: ", e);
            }
        }
    }

    public static void trace(Runnable action) {
        if (TRACE) {
            try {
                action.run();
            } catch (Exception e) {
                logger.info("Error running trace action: ", e);
            }
        }
    }
}
