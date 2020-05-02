package SmartCity;

public abstract class Crossroad {
	public abstract void addCarToQueue(String carName, int adjacentOsmWayId);
	public abstract void addPedestrianToQueue(String pedestrianName, int adjacentOsmWayId);
	public abstract OptimizationResult requestOptimizations();
	public abstract boolean isLightGreen(int adjacentOsmWayId);
}
