import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TrafficLightAgent extends Agent {
    boolean isRed = true;
    protected void setup(){
        System.out.println("I'm a light!");
//        System.out.println("Red light.");
        Behaviour ReceiveMessage = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg != null)
                {
                    System.out.println("Received message: " + msg.getContent());
                    block(200);
                }
            }
        };
        addBehaviour(ReceiveMessage);
//        Behaviour LightSwitch = new DelayBehaviour(this, 15000) {
//            @Override
//            public void handleElapsedTimeout() {
//                isRed = !isRed;
//                if(isRed) System.out.println("Red Light.");
//                else System.out.println("Green Light.");
//            }
//        };
//        addBehaviour(LightSwitch);
    }

    protected void takeDown(){
        super.takeDown();
    }
}
