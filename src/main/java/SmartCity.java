import jade.Boot;
import smartcity.MainContainerAgent;

public class SmartCity {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"-agents",
                    MainContainerAgent.class.getName() + ":" + MainContainerAgent.class.getCanonicalName()};
        }

        Boot.main(args);
    }

}
