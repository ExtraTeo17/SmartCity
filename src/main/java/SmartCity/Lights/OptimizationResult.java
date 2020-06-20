package SmartCity.Lights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import SmartCity.Stations.PedestrianArrivalInfo;

public class OptimizationResult {
	private List<String> carsFreeToProceedNames = new ArrayList<>();
	private List<Pair<String,List<String>>> busesAndPedestriansFreeToProceedNames = new ArrayList<>();

	public List<String> carsFreeToProceed() {
		return carsFreeToProceedNames;
	}
	
	public void addCarGrantedPassthrough(String carName) {
		carsFreeToProceedNames.add(carName);
	}
	public void addBusAndPedestrianGrantedPassthrough(String busAgentName,   List<String> pedestrians ) {
		busesAndPedestriansFreeToProceedNames.add(new Pair<String,List<String>>(busAgentName,pedestrians));
	}
	
	public  List<Pair<String,List<String>>> busesAndPedestriansFreeToProceed() {
		return busesAndPedestriansFreeToProceedNames;
	}
	
	public static OptimizationResult empty() {
		return new OptimizationResult();
	}
}
