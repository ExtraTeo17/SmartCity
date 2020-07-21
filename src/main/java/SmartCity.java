import jade.Boot;

public class SmartCity {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"-agents", "SmartCityAgent:smartcity.SmartCityAgent"};
        }

        Boot.main(args);
    }

}
