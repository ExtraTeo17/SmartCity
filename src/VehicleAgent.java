import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class VehicleAgent extends Agent {
    Car car;

    public VehicleAgent() {
        car = new Car();
    }

    public VehicleAgent(Car car) {
        this.car = car;
    }

    protected void setup() {
        System.out.println("I'm a car!");
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
                car.Move();
                System.out.println("Position: " + car.Position);
                block(1000);
            }
        };
        addBehaviour(move);
    }

    protected void takeDown() {
        super.takeDown();
    }
}
