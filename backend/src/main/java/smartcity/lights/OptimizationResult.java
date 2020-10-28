package smartcity.lights;

import org.javatuples.Pair;
import routing.core.Position;
import smartcity.TimeProvider;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private final List<String> carsFreeToProceedNames = new ArrayList<>();
    private final List<Pair<String, List<String>>> busesAndPedestriansFreeToProceedNames = new ArrayList<>();
    private boolean shouldNotifyCarAboutStartOfTrafficJamOnThisLight = false;
    private boolean shouldNotifyCarAboutStopOfTrafficJamOnThisLight = false;
    private double lengthOfJam = 0;
    private long osmWayId = 0;
    private Position jammedLightPosition = null;
    private String agentStuckInJam = null;
    private final int extendTimeSeconds;
    private final int defaultExecutionDelay;

    public OptimizationResult(int extendTimeSeconds) {
        this.extendTimeSeconds = extendTimeSeconds;
        this.defaultExecutionDelay = extendTimeSeconds * 1000 / TimeProvider.TIME_SCALE;
    }

    public long getOsmWayId() {
        return osmWayId;
    }

    public static OptimizationResult empty() {
        return new OptimizationResult(0);
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
        busesAndPedestriansFreeToProceedNames.add(new Pair<String, List<String>>(busAgentName, pedestrians));
    }

    public List<Pair<String, List<String>>> busesAndPedestriansFreeToProceed() {
        return busesAndPedestriansFreeToProceedNames;
    }

    public void setShouldNotifyCarAboutStartOfTrafficJamOnThisLight(double jammedLightLat, double jammedLightLon,
                                                                    int numerOfCarsInTheQueue, long osmWayId) {
        shouldNotifyCarAboutStartOfTrafficJamOnThisLight = true;
        jammedLightPosition = Position.of(jammedLightLat, jammedLightLon);
        this.osmWayId = osmWayId;
        //TODO: change 2 na liczbé samochodów które przejzdzaja podczas jednego swiatla. Oraz change how long is green and red
        lengthOfJam = Math.floor(Math.floor(numerOfCarsInTheQueue / defaultExecutionDelay) *
                ((defaultExecutionDelay + defaultExecutionDelay) +
                        (defaultExecutionDelay + defaultExecutionDelay + extendTimeSeconds)) / 2); // TODO: Magic numbers

    }

    public final double getLengthOfJam() {
        return lengthOfJam;
    }

    public final Position getJammedLightPosition() {
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
