package SmartCity;

import java.util.Dictionary;

public class SimpleCrossroad extends Crossroad {
	
	private Dictionary<Integer, Light> lights;
	private SimpleLightGroup group1;
	private SimpleLightGroup group2;

	@Override
	public void addCarToQueue(String carName, int adjacentOsmWayId) {
		lights.get(adjacentOsmWayId).addCarToQueue(carName);
	}

	@Override
	public void addPedestrianToQueue(String pedestrianName, int adjacentOsmWayId) {
		// TODO Auto-generated method stub
	}

	@Override
	public OptimizationResult requestOptimizations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLightGreen(int adjacentOsmWayId) {
		return lights.get(adjacentOsmWayId).isGreen();
	}
}
