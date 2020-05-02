package SmartCity;

import java.util.Queue;

import Agents.LightColor;

public class Light {
	
	private LightColor color;
	private Queue<String> carQueue;

	public void addCarToQueue(String carName) {
		carQueue.add(carName);
	}
	
	public String removeCarFromQueue() {
		return carQueue.remove();
	}

	public boolean isGreen() {
		return color == LightColor.GREEN;
	}
}
