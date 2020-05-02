package LightStrategies;

import java.util.List;

import Agents.LightManager;
import SmartCity.Crossroad;
import SmartCity.OptimizationResult;
import SmartCity.SimpleCrossroad;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class LightManagerStrategy extends LightStrategy {
	
	private final static String ADJACENT_OSM_WAY_ID = "adjacentOsmWayId";
	
	private Crossroad crossroad = new SimpleCrossroad();
	
    @Override
    public void ApplyStrategy(final LightManager agent) {
        Behaviour communication = new SimpleBehaviour() {

            public void action() {
                ACLMessage rcv = agent.receive();
                if (rcv != null) {
                	handleMessageFromCar(rcv);
                } else
                    block();
            }
            
            public boolean done() {
                return true;
            }
            
            private void handleMessageFromCar(ACLMessage rcv) {
                switch (rcv.getPerformative()) {
	                case ACLMessage.INFORM:
	                    // System.out.println("Manager: Car is close");
	                    //or
	                    //people came to traffic
	                    break;
	                case ACLMessage.REQUEST_WHEN:
	                    System.out.println("Manger: Cat is waiting");
	                    handleCarArrival(rcv);
	                    //Answer agree
	                    break;
	                default:
	                    System.out.println("Wait");
	            }
            }
            
            private void handleCarArrival(ACLMessage rcv) {
            	if (crossroad.isLightGreen(getIntParameter(rcv, ADJACENT_OSM_WAY_ID))) {
            		answerCanProceed(getCarName(rcv));
            	} else {
	            	crossroad.addCarToQueue(
	            			getCarName(rcv),
	            			getIntParameter(rcv, ADJACENT_OSM_WAY_ID));
            	}
            }
        };
        
        Behaviour checkState = new TickerBehaviour(agent, 1) {
			
			@Override
			protected void onTick() {
                //for all Light check
                //check if time from last green > written time
                // if so, put in the queue
                //if not
                // check count of people (rememeber about 2 person on pedestrian light= 1 car)
                // if queue is empty
                // apply strategy
                //for elemnts in queue (if there are elements in queue, make green)
				
				OptimizationResult result = crossroad.requestOptimizations();
				handleOptimizationResult(result);
			}
			
			private void handleOptimizationResult(OptimizationResult result) {
				List<String> carNames = result.carsFreeToProceed();
				for (String carName : carNames) {
					answerCanProceed(carName);
				}
			}
        };
        
        agent.addBehaviour(communication);
        agent.addBehaviour(checkState);
    }
    
    private void answerCanProceed(String carName) {
    	// TODO: Katsiaryna <33
    }
    
    private int getIntParameter(ACLMessage rcv, String param) {
    	return Integer.parseInt(rcv.getUserDefinedParameter(param));
    }
    
    private String getCarName(ACLMessage rcv) {
    	return rcv.getSender().getLocalName();
    }
}
