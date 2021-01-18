package smartcity.lights;

import org.javatuples.Pair;
import routing.core.IGeoPosition;
import routing.core.Position;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    public static final double MILLISECONDS_IN_SECONDS = 1000.0;
    private final List<String> carsFreeToProceedNames = new ArrayList<>();
    private final List<Pair<String, List<String>>> busesAndPedestriansFreeToProceedNames = new ArrayList<>();
    private final int extendTimeSeconds;
    private final int defaultExecutionDelay;

    private boolean shouldNotifyCarAboutStartOfTrafficJamOnThisLight = false;
    private boolean shouldNotifyCarAboutStopOfTrafficJamOnThisLight = false;
    private double lengthOfJam = 0;
    private long osmWayId = 0;
    private IGeoPosition jammedLightPosition = null;
    private String agentStuckInJam = null;

    public OptimizationResult(int extendTimeSeconds, int defaultExecutionDelay) {
        this.extendTimeSeconds = extendTimeSeconds;
        this.defaultExecutionDelay = defaultExecutionDelay;
    }

    public long getOsmWayId() {
        return osmWayId;
    }

    public static OptimizationResult empty() {
        return new OptimizationResult(0, 0);
    }

    public List<String> carsFreeToProceed() {
        return carsFreeToProceedNames;
    }

    public void addCarGrantedPassthrough(String carName) {
        carsFreeToProceedNames.add(carName);
    }

    public void setCarStuckInJam(final String carStuckInJam) {
        agentStuckInJam = carStuckInJam;
    }

    public void addBusAndPedestrianGrantedPassthrough(String busAgentName, List<String> pedestrians) {
        busesAndPedestriansFreeToProceedNames.add(new Pair<>(busAgentName, pedestrians));
    }

    public List<Pair<String, List<String>>> busesAndPedestriansFreeToProceed() {
        return busesAndPedestriansFreeToProceedNames;
    }

    public void setShouldNotifyCarAboutStartOfTrafficJamOnThisLight(IGeoPosition jammedLightPosition, int numerOfCarsInTheQueue,
                                                                    long osmWayId) {
        shouldNotifyCarAboutStartOfTrafficJamOnThisLight = true;
        this.osmWayId = osmWayId;
        this.jammedLightPosition = jammedLightPosition;
        //TODO: Change 2 for number of cars that passes through 1 light
        //  And change how long is green and red
        lengthOfJam = Math.floor((numerOfCarsInTheQueue * MILLISECONDS_IN_SECONDS / defaultExecutionDelay) *
                ((defaultExecutionDelay + defaultExecutionDelay) +
                        (defaultExecutionDelay + defaultExecutionDelay + extendTimeSeconds)) / 2);
    }

    public final double getLengthOfJam() {
        return lengthOfJam;
    }

    public final IGeoPosition getJammedLightPosition() {
        if (jammedLightPosition == null) {
            throw new RuntimeException("No light has been reported as jammed!");
        }
        return jammedLightPosition;
    }

    public final boolean shouldNotifyCarAboutStartOfTrafficJamOnThisLight() {
        return shouldNotifyCarAboutStartOfTrafficJamOnThisLight;
    }

    public final boolean shouldNotifyCarAboutStopOfTrafficJamOnThisLight() {
        return shouldNotifyCarAboutStopOfTrafficJamOnThisLight;
    }

    public void setShouldNotifyCarAboutEndOfTrafficJamOnThisLight(double jammedLightLat, double jammedLightLon, long osmWayId) {
        shouldNotifyCarAboutStopOfTrafficJamOnThisLight = true;
        jammedLightPosition = Position.of(jammedLightLat, jammedLightLon);
        this.osmWayId = osmWayId;
    }

    public String getAgentStuckInJam() {
        return agentStuckInJam;
    }
}
