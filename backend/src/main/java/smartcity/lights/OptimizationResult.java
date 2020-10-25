package smartcity.lights;

import org.javatuples.Pair;

import routing.core.Position;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private final List<String> carsFreeToProceedNames = new ArrayList<>();
    //private final List<Pair<CROSSROAD, Boolean>> trafficJams = new ArrayList<>();
    private final List<Pair<String, List<String>>> busesAndPedestriansFreeToProceedNames = new ArrayList<>();
	private boolean shouldNotifyCarAboutTrafficJamOnThisLight = false;
    private double lengthOfJam=0;
	private Position jammedLightPosition = null;

    public static OptimizationResult empty() {
        return new OptimizationResult();
    }

    public List<String> carsFreeToProceed() {
        return carsFreeToProceedNames;
    }

    public void addCarGrantedPassthrough(String carName) {
        carsFreeToProceedNames.add(carName);
    }

    public void addBusAndPedestrianGrantedPassthrough(String busAgentName, List<String> pedestrians) {
        busesAndPedestriansFreeToProceedNames.add(new Pair<String, List<String>>(busAgentName, pedestrians));
    }

    public List<Pair<String, List<String>>> busesAndPedestriansFreeToProceed() {
        return busesAndPedestriansFreeToProceedNames;
    }
   // public List<Pair<CROSSROAD, Boolean>> getTrafficJamsInfo(){return trafficJams; }

	public void setShouldNotifyCarAboutTrafficJamOnThisLight(double jammedLightLat, double jammedLightLon, int numerOfCarsInTheQueue) {
		shouldNotifyCarAboutTrafficJamOnThisLight = true;
		jammedLightPosition = Position.of(jammedLightLat, jammedLightLon);
		//TODO: change 2 na liczbé samochodów które przejzdzaja podczas jednego swiatla. Oraz change how long is green and red
		lengthOfJam = Math.floor(Math.floor(numerOfCarsInTheQueue/2)*((5 + 5)+(5+5+2.5))/2);

	}
	public final double getLengthOfJam(){return lengthOfJam;}

	public final Position getJammedLightPosition() throws Exception {
		if (jammedLightPosition == null) {
			throw new Exception("No light has been reported as jammed!");
		}
		return jammedLightPosition;
	}
	
	public final boolean shouldNotifyCarAboutTrafficJamOnThisLight() {
		return shouldNotifyCarAboutTrafficJamOnThisLight;
	}
}
