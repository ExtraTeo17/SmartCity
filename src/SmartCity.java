import jade.Boot;
import javax.swing.*;

public class SmartCity {
	private final static String[] defaultJadeArgs = { "-gui", "Light8:Agents.TrafficLightAgent;kotik:Agents.VehicleAgent(BasicCar)" };

	public static void main(String[] args) {
		Boot.main(parseJadeArguments(args));
		displayMap();
	}
	
	private final static String[] parseJadeArguments(String[] args) {
		if (args.length == 0) {
			return defaultJadeArgs;
		} else {
			return args;
		}
	}
	
	private static void displayMap() {
		JFrame frame = new JFrame();
		frame.setSize(500, 400);
		frame.setLayout(null);
		frame.setVisible(true);
	}
}
