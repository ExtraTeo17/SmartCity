package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import events.LightSwitcherStartedEvent;
import events.web.SwitchLightsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.ITimeProvider;
import smartcity.config.abstractions.ILightConfigContainer;
import smartcity.task.data.ISwitchLightsContext;
import utilities.Siblings;

import java.util.function.Function;

public class LightSwitcher implements Function<ISwitchLightsContext, Integer> {
    private static final int CAR_TO_PEDESTRIAN_LIGHT_RATE = 2;
    private final Logger logger;

    private final ITimeProvider timeProvider;
    private final ILightConfigContainer configContainer;
    private final EventBus eventBus;

    private final int extendTimeSeconds;
    private final int defaultExecutionDelay;

    private Long lightOsmId;
    private SimpleLightGroup greenLights;
    private SimpleLightGroup redLights;

    @Inject
    LightSwitcher(ITimeProvider timeProvider,
                  ILightConfigContainer configContainer,
                  EventBus eventBus,
                  @Assisted("managerId") int managerId,
                  @Assisted("extendTime") int extendTimeSeconds,
                  @Assisted Siblings<SimpleLightGroup> lights) {
        this.timeProvider = timeProvider;
        this.configContainer = configContainer;
        this.eventBus = eventBus;

        this.extendTimeSeconds = extendTimeSeconds;
        this.defaultExecutionDelay = extendTimeSeconds * 1000 / timeProvider.getTimeScale();
        setLights(lights);

        this.logger = LoggerFactory.getLogger("LightSwitcher" + managerId);
        eventBus.post(new LightSwitcherStartedEvent(managerId, defaultExecutionDelay));
    }

    private void setLights(Siblings<SimpleLightGroup> lights) {
        var firstIter = lights.first.iterator();
        var secondIter = lights.second.iterator();

        Light anyLight;
        boolean isFirstGroupGreen;
        if (firstIter.hasNext()) {
            anyLight = firstIter.next();
            isFirstGroupGreen = anyLight.isGreen();
        }
        else if (secondIter.hasNext()) {
            anyLight = secondIter.next();
            isFirstGroupGreen = !anyLight.isGreen();
        }
        else {
            throw new IllegalArgumentException("Both groups does not have any lights");
        }

        if (isFirstGroupGreen) {
            this.greenLights = lights.first;
            this.redLights = lights.second;
        }
        else {
            this.redLights = lights.first;
            this.greenLights = lights.second;
        }

        this.lightOsmId = anyLight.getOsmLightId();
    }

    @Override
    public Integer apply(ISwitchLightsContext context) {
        if (configContainer.isLightStrategyActive()) {
            if (context.haveNotExtendedYet()) {
                if (shouldExtendGreenLightBecauseOfObjectsOnLight()) {
                    logger.debug("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                    context.setNotExtendedGreen(false);
                    return defaultExecutionDelay;
                }
                else if (shouldExtendBecauseOfFarAwayQueue()) {
                    logger.debug("-------------------------------------shouldExtendBecauseOfFarAwayQueue--------------");
                    context.setNotExtendedGreen(false);
                    return defaultExecutionDelay;
                }
            }
            else {
                context.setNotExtendedGreen(true);
            }
        }

        switchLights();
        logger.debug("Switched light at: " + timeProvider.getCurrentSimulationTime());
        eventBus.post(new SwitchLightsEvent(lightOsmId));

        return defaultExecutionDelay;
    }

    private boolean shouldExtendGreenLightBecauseOfObjectsOnLight() {
        int greenGroupObjects = 0;
        int redGroupObjects = 0;

        for (Light light : greenLights) {
            redGroupObjects += light.pedestrianQueue.size();
            greenGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE * light.carQueue.size();
        }
        // Reverse is intended - each group is opposite to one another
        for (Light light : redLights) {
            greenGroupObjects += light.pedestrianQueue.size();
            redGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE * light.carQueue.size();
        }

        boolean result = greenGroupObjects > redGroupObjects;
        if (result) {
            logger.debug("LM:CROSSROAD HAS PROLONGED GREEN LIGHT FOR " + greenGroupObjects + " CARS AS OPPOSED TO " + redGroupObjects);
        }

        return result;
    }

    private boolean shouldExtendBecauseOfFarAwayQueue() {
        var currentTime = timeProvider.getCurrentSimulationTime();
        var currentTimePlusExtend = currentTime.plusSeconds(extendTimeSeconds);

        int greenGroupObjects = 0;
        int redGroupObjects = 0;
        for (Light light : greenLights) {
            for (var time : light.farAwayCarMap.values()) {
                if (currentTimePlusExtend.isAfter(time) && time.isBefore(currentTime)) {
                    greenGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE;
                }
            }
            for (var time : light.farAwayPedestrianMap.values()) {
                if (currentTimePlusExtend.isAfter(time) && time.isBefore(currentTime)) {
                    ++redGroupObjects;
                }
            }
        }

        for (Light light : redLights) {
            for (var time : light.farAwayPedestrianMap.values()) {
                if (currentTimePlusExtend.isAfter(time) && time.isBefore(currentTime)) {
                    ++greenGroupObjects;
                }
            }
            for (var time : light.farAwayCarMap.values()) {
                if (currentTimePlusExtend.isAfter(time) && time.isBefore(currentTime)) {
                    redGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE;
                }
            }
        }

        return greenGroupObjects > redGroupObjects;
    }

    private void switchLights() {
        // TODO: Can include yellow somehow?
        greenLights.switchLights();
        redLights.switchLights();

        var tmp = greenLights;
        greenLights = redLights;
        redLights = tmp;
    }
}