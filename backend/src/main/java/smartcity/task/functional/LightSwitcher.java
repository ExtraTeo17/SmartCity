package smartcity.task.functional;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.ITimeProvider;
import smartcity.TimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.lights.core.Light;
import smartcity.task.data.ISwitchLightsContext;

import java.util.Collection;
import java.util.function.Function;

class LightSwitcher implements Function<ISwitchLightsContext, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(LightSwitcher.class);

    private final ITimeProvider timeProvider;
    private final ConfigContainer configContainer;

    private final int extendTimeSeconds;
    private final int defaultExecutionDelay;
    private final Collection<Light> lights;

    @Inject
    public LightSwitcher(ITimeProvider timeProvider,
                         ConfigContainer configContainer,
                         @Assisted int extendTimeSeconds,
                         @Assisted Collection<Light> lights) {
        this.timeProvider = timeProvider;
        this.configContainer = configContainer;
        this.extendTimeSeconds = extendTimeSeconds;
        this.defaultExecutionDelay = extendTimeSeconds * 1000 / TimeProvider.TIME_SCALE;
        this.lights = lights;
    }

    @Override
    public Integer apply(ISwitchLightsContext context) {
        if (configContainer.isLightStrategyActive()) {
            if (!context.haveAlreadyExtendedGreen()) {
                if (shouldExtendGreenLightBecauseOfCarsOnLight()) {
                    logger.info("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                    context.setExtendedGreen(true);
                    return defaultExecutionDelay;
                }
                else if (shouldExtendBecauseOfFarAwayQueue()) {
                    logger.info("-------------------------------------shouldExtendBecauseOfFarAwayQueue--------------");
                    context.setExtendedGreen(true);
                    return defaultExecutionDelay;
                }
            }
            else {
                context.setExtendedGreen(false);
            }
        }

        lights.forEach(Light::switchLight);
        return defaultExecutionDelay;
    }

    boolean shouldExtendGreenLightBecauseOfCarsOnLight() {
        int greenGroupCars = 0;
        int redGroupCars = 0;
        for (Light light : lights) {
            // temporarily only close queue
            redGroupCars += light.getRedGroupSize();
            greenGroupCars += light.getGreenGroupSize();
        }
        if (greenGroupCars > redGroupCars) {
            logger.info("LM:CROSSROAD HAS PROLONGED GREEN LIGHT FOR " + greenGroupCars + " CARS AS OPPOSED TO " + redGroupCars);
        }

        // TODO: should check if two base green intervals have passed (also temporary, because it also sucks)
        return greenGroupCars > redGroupCars;
    }

    boolean shouldExtendBecauseOfFarAwayQueue() {
        for (Light light : lights) {
            var currentTime = timeProvider.getCurrentSimulationTime();
            var currentTimePlusExtend = currentTime.plusSeconds(extendTimeSeconds);

            var timeCollection = light.getFarAwayTimeCollection();
            for (var time : timeCollection) {
                if (currentTimePlusExtend.isAfter(time)) {
                    logger.info("Extending, time=" + time + ", currentTime=" + currentTime);
                    return true;
                }
            }
        }

        return false;
    }

    ;
}
