package smartcity.task.functional;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import events.web.SwitchLightsEvent;
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
    private final Logger logger;

    private final ITimeProvider timeProvider;
    private final ConfigContainer configContainer;
    private final EventBus eventBus;

    private final int extendTimeSeconds;
    private final int defaultExecutionDelay;
    private final Collection<Light> lights;
    private final Long lightOsmId;

    @Inject
    LightSwitcher(ITimeProvider timeProvider,
                  ConfigContainer configContainer,
                  EventBus eventBus,
                  @Assisted("managerId") int managerId,
                  @Assisted("extendTime") int extendTimeSeconds,
                  @Assisted Collection<Light> lights) {
        this.timeProvider = timeProvider;
        this.configContainer = configContainer;
        this.eventBus = eventBus;

        this.extendTimeSeconds = extendTimeSeconds;

        this.defaultExecutionDelay = extendTimeSeconds * 1000 / TimeProvider.TIME_SCALE;
        this.lights = lights;
        this.lightOsmId = lights.iterator().next().getOsmLightId();
        this.logger = LoggerFactory.getLogger("LightSwitcher" + managerId);
    }

    @Override
    public Integer apply(ISwitchLightsContext context) {
        if (configContainer.isLightStrategyActive()) {
            if (!context.haveAlreadyExtendedGreen()) {
                if (shouldExtendGreenLightBecauseOfCarsOnLight()) {
                    logger.debug("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                    context.setExtendedGreen(true);
                    return defaultExecutionDelay;
                }
                else if (shouldExtendBecauseOfFarAwayQueue()) {
                    logger.debug("-------------------------------------shouldExtendBecauseOfFarAwayQueue--------------");
                    context.setExtendedGreen(true);
                    return defaultExecutionDelay;
                }
            }
            else {
                context.setExtendedGreen(false);
            }
        }

        // TODO: Can include yellow somehow?
        lights.forEach(Light::switchLight);
        logger.debug("Switched light at: " + timeProvider.getCurrentSimulationTime());
        eventBus.post(new SwitchLightsEvent(lightOsmId));

        return defaultExecutionDelay;
    }

    private boolean shouldExtendGreenLightBecauseOfCarsOnLight() {
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

    private boolean shouldExtendBecauseOfFarAwayQueue() {
        for (Light light : lights) {
            var currentTime = timeProvider.getCurrentSimulationTime();
            var currentTimePlusExtend = currentTime.plusSeconds(extendTimeSeconds);

            var timeCollection = light.getFarAwayTimeCollection();
            for (var time : timeCollection) {
                if (currentTimePlusExtend.isAfter(time)) {
                    logger.trace("Extending, time=" + time.toLocalTime() + ", currentTime=" + currentTime.toLocalTime());
                    return true;
                }
            }
        }

        return false;
    }
}