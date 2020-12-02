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
import utilities.ConditionalExecutor;
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
        var extendTime = getExtendTime(context.haveAlreadyExtended());
        if (extendTime > 0) {
            context.setAlreadyExtendedGreen(true);
            return extendTime;
        }

        context.setAlreadyExtendedGreen(false);
        switchLights();

        return defaultExecutionDelay;
    }

    private int getExtendTime(boolean hadPreviouslyExtended) {
        if (configContainer.isLightStrategyActive()) {
            var closeGroups = getLightGroupsOnLight();
            if (shouldExtendByGroups(hadPreviouslyExtended, closeGroups)) {
                logger.info("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                return defaultExecutionDelay;
            }

            if (closeGroups[1] > 0) {
                return 0;
            }

            var farGroups = getLightGroupsInFarawayQueue();
            if (shouldExtendByGroups(hadPreviouslyExtended, farGroups)) {
                logger.info("-------------------------------------shouldExtendBecauseOfFarAwayQueue--------------");
                return defaultExecutionDelay;
            }
        }

        return 0;
    }

    private int[] getLightGroupsOnLight() {
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

        return new int[]{greenGroupObjects, redGroupObjects};
    }

    private boolean shouldExtendByGroups(boolean hadPreviouslyExtended, int[] groups) {
        if (hadPreviouslyExtended) {
            return groups[0] > 0 && groups[1] == 0;
        }

        return groups[0] > groups[1];
    }

    private int[] getLightGroupsInFarawayQueue() {
        var currentTime = timeProvider.getCurrentSimulationTime();
        var currentTimePlusExtend = currentTime.plusSeconds(extendTimeSeconds);

        int greenGroupObjects = 0;
        int redGroupObjects = 0;
        for (Light light : greenLights) {
            var lightGroups = light.getFarawayCarsAndPedestriansWithinInterval(currentTime, currentTimePlusExtend);
            greenGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE * lightGroups[0];
            redGroupObjects += lightGroups[1];
        }

        for (Light light : redLights) {
            var lightGroups = light.getFarawayCarsAndPedestriansWithinInterval(currentTime, currentTimePlusExtend);
            greenGroupObjects += lightGroups[1];
            redGroupObjects += CAR_TO_PEDESTRIAN_LIGHT_RATE * lightGroups[0];
        }

        return new int[]{greenGroupObjects, redGroupObjects};
    }

    private void switchLights() {
        // TODO: Can include yellow somehow?
        greenLights.switchLights();
        redLights.switchLights();

        var tmp = greenLights;
        greenLights = redLights;
        redLights = tmp;

        ConditionalExecutor.debug(() -> logger.debug("Switched light at: " + timeProvider.getCurrentSimulationTime()));
        eventBus.post(new SwitchLightsEvent(lightOsmId));
    }
}