package smartcity.lights;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private List<String> carsFreeToProceedNames = new ArrayList<>();
    private List<Pair<String, List<String>>> busesAndPedestriansFreeToProceedNames = new ArrayList<>();

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
}
