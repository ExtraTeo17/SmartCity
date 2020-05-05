package LightStrategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

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
	private final static String TYPE = "type";
	public final static String PEDESTRIAN = "Pedestrian";
	public final static String VEHICLE = "Vehicle";
	
	private Crossroad crossroad;
	
    public LightManagerStrategy(Node crossroad, Long managerId) {
		this.crossroad = new SimpleCrossroad(crossroad, managerId);
	}

	@Override
    public void ApplyStrategy(final LightManager agent) {
		crossroad.startLifetime();
		
        Behaviour communication = new SimpleBehaviour() {
            
            public boolean done() {
                return true;
            }

            public void action() {
                ACLMessage rcv = agent.receive();
                if (rcv != null) {
                	handleMessageFromRecipient(rcv);
                } else
                    block();
            }
            
            private void handleMessageFromRecipient(ACLMessage rcv) {
            	String recipientType = rcv.getUserDefinedParameter(TYPE);
            	switch (recipientType) {
            	case VEHICLE:
            		handleMessageFromVehicle(rcv);
            		break;
            	case PEDESTRIAN:
            		handleMessageFromPedestrian(rcv);
            		break;
            	}
            }
            
            private void handleMessageFromVehicle(ACLMessage rcv) {
                switch (rcv.getPerformative()) {
	                case ACLMessage.INFORM:
	                    System.out.println("Manager: Car has passed the previous crossroad.");
	                    handleVehicleOnItsWay(rcv);
	                case ACLMessage.REQUEST_WHEN:
	                    System.out.println("Manager: Car is waiting");
	                    handleVehicleArrival(rcv);
	                    //Answer agree
	                    break;
	                case ACLMessage.AGREE:
	                	//Vehicle answered agree and has successfully passed the crossroad on green light.
	                	//Can remove vehicle from the queue.
	                default:
	                    System.out.println("Wait");
	            }
            }
            
            private void handleVehicleOnItsWay(ACLMessage rcv) {
				// TODO Auto-generated method stub
				
			}

			private void handleVehicleArrival(ACLMessage rcv) {
				// TODO Auto-generated method stub
				
			}

			private void handleMessageFromPedestrian(ACLMessage rcv) {
                switch (rcv.getPerformative()) {
	                case ACLMessage.INFORM:
	                    System.out.println("Manager: Pedestrian has passed the previous crossing.");
	                    handlePedestrianOnItsWay(rcv);
	                case ACLMessage.REQUEST_WHEN:
	                    System.out.println("Manager: Pedestrian is waiting.");
	                    handlePedestrianArrival(rcv);
	                    //Answer agree
	                    break;
	                case ACLMessage.AGREE:
	                	//Pedestrian answered agree and has successfully passed the crossing on green light.
	                	//Can remove pedestrian from the queue.
	                default:
	                    System.out.println("Wait");
	            }
            }

			private void handlePedestrianOnItsWay(ACLMessage rcv) {
				// TODO Auto-generated method stub
				
			}

			private void handlePedestrianArrival(ACLMessage rcv) {
				// TODO Auto-generated method stub
				
			}
        };
        
        Behaviour checkState = new TickerBehaviour(agent, 1000) {
			
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
    
    public void drawCrossroad(HashSet lightSet, WaypointPainter<Waypoint> painter) {
    	crossroad.draw(lightSet, painter);
    }
}
