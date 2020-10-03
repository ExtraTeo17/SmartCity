package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.StaticConfig;

public class ConditionalExecutor {
    private final static Logger logger = LoggerFactory.getLogger(ConditionalExecutor.class);

    public static void debug(Runnable action) {
        if (StaticConfig.DEBUG || StaticConfig.TRACE) {
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
