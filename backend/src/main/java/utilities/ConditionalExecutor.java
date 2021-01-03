package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static smartcity.config.StaticConfig.DEBUG;
import static smartcity.config.StaticConfig.TRACE;

/**
 * Used for debugging purposes.
 * Methods of this class are similar to preprocessor directives.
 */
public class ConditionalExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ConditionalExecutor.class);


    /**
     * Executed when {@link smartcity.config.StaticConfig#DEBUG} or {@link smartcity.config.StaticConfig#TRACE} is true.
     *
     * @param action Action to invoke
     */
    public static void debug(Runnable action) {
        debug(action, true);
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

    /**
     * Executed when  {@link smartcity.config.StaticConfig#TRACE} is true.
     *
     * @param action Action to invoke
     */
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
