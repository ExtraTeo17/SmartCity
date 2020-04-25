package LightStrategies;

import Agents.LightColor;
import Agents.LightManager;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class LightManagerStrategy extends LightStrategy {
    @Override
    public void ApplyStrategy(final LightManager agent) {
        Behaviour communication=new SimpleBehaviour( )
        {

            public void action()
            {
                ACLMessage rcv = agent.receive();
                ACLMessage response;

                if (rcv != null) {

                    switch (rcv.getPerformative()) {
                        case ACLMessage.INFORM:
                               // System.out.println("Manager: Car is close");
                                //or
                           //people came to traffic
                            break;
                        case ACLMessage.REQUEST_WHEN:
                            System.out.println("Manger: Cat is waiting");
                            //Answer agree
                        default:
                            System.out.println("Wait");

                    }

                }
                else
                    block();

            }
            public boolean done(){
                return   true;
            }
        };
        Behaviour check_state=new SimpleBehaviour( )
        {

            public void action()
            {

                //for all Light check
                //check if time from last green > written time
                // if so, put in the queue
                //if not
                // check count of people (rememeber about 2 person on pedestrian light= 1 car)
                // if queue is empty
                // apply strategy
                //for elemnts in queue (if there are elements in queue, make green)
            }
            public boolean done(){
                return   true;
            }
        };


    }
}
