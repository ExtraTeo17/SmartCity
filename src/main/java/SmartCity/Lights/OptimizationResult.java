package SmartCity.Lights;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
	private List<String> carsFreeToProceedNames = new ArrayList<>();

	public List<String> carsFreeToProceed() {
		return carsFreeToProceedNames;
	}
	
	public void addCarGrantedPassthrough(String carName) {
		carsFreeToProceedNames.add(carName);
	}
	
	public static OptimizationResult empty() {
		return new OptimizationResult();
	}
}
