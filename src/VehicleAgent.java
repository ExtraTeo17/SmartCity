import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.scene.effect.Light;

public class VehicleAgent extends Agent {
    Car car;
    LightColor nextLightColor;
    public VehicleAgent() {
        car = new Car();
    }

    public VehicleAgent(Car car) {
        this.car = car;
    }

    protected void setup() {
        System.out.println("I'm a car with a name: " + getLocalName());
        Behaviour move = new CyclicBehaviour() {
            @Override
            public void action() {
                if (car.Position == 0) {
                    AID dest = new AID("Light8", AID.ISLOCALNAME);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("On my way.");
                    msg.addReceiver(dest);
                    send(msg);
                }
                if(car.Position == 8) //When car arrives at the traffic light
                {
                    if(nextLightColor == LightColor.GREEN){
                        System.out.println("Passing green light.");
                        AID dest = new AID("Light8", AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Pass");
                        msg.addReceiver(dest);
                        send(msg);
                        car.Move();
                    }
                    else
                        System.out.println("Waiting for green.");
                }
                else car.Move();
                System.out.println("Position: " + car.Position);
                block(1000);
            }
        };
        addBehaviour(move);

        Behaviour receiveMessages = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("Message from "+ msg.getSender().getLocalName() + ": " + msg.getContent());
                    // receiving next color light
                    if(msg.getContent().equals("Green"))
                    {
                        nextLightColor = LightColor.GREEN;
                    }
                    else if(msg.getContent().equals("Red"))
                    {
                        nextLightColor = LightColor.RED;
                    }
                    block(200);
                }
            }
        };
        addBehaviour(receiveMessages);
    }

    protected void takeDown() {
        super.takeDown();
    }
}
