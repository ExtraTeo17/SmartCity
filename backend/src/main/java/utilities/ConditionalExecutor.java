package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.StaticConfig;

import java.util.function.BooleanSupplier;

public class ConditionalExecutor {
    private final static Logger logger = LoggerFactory.getLogger(ConditionalExecutor.class);

    public static void debug(Runnable action) {
        debug(action, false);
    }

    public static void debug(Runnable action, boolean necessaryCondition) {
        if ((StaticConfig.DEBUG || StaticConfig.TRACE) && necessaryCondition) {
            try {
                action.run();
            } catch (Exception e) {
                logger.info("Error running debug action: ", e);
            }
        }
    }

    public static void trace(Runnable action) {
        if (StaticConfig.TRACE) {
            try {
                action.run();
            }
            catch (Exception e){
                logger.info("Error running trace action: ", e);
            }
        }
    }
}
