package Agents;

import jade.core.AID;
import jade.core.Agent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.time.Instant;
import java.util.List;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMStation;
import SmartCity.Lights.OptimizationResult;
import SmartCity.Stations.StationStrategy;

public class StationAgent extends Agent {
	private final StationStrategy stationStrategy;
	
	//private final OSMStation stationOSMNode;
	private final long agentId;
	
	public StationAgent( OSMStation stationOSMNode, final long agentId) { // REMEMBER TO PRUNE BEYOND CIRCLE
		this.stationStrategy = new StationStrategy(stationOSMNode,agentId);	
		
		this.agentId = agentId;
		Behaviour communication = new CyclicBehaviour() {
			@Override
			public void action() {
				
				ACLMessage rcv = receive();
				if (rcv != null) {
					String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
					if (type == MessageParameter.BUS) {
						handleMessageFromBus(rcv);
						
					}
					else if (type == MessageParameter.PEDESTRIAN) {
						handleMessageFromPedestrian(rcv);
						
					}
				}
				block(100);
			}
			

			 private int getIntParameter(ACLMessage rcv, String param) {
			        return Integer.parseInt(rcv.getUserDefinedParameter(param));
			    }
			    private Instant getInstantParameter(ACLMessage rcv, String param) {
			        return Instant.parse(rcv.getUserDefinedParameter(param));
			    }


			private void handleMessageFromBus(ACLMessage rcv) {
				if (rcv.getPerformative() == ACLMessage.INFORM) {
				
					String agentBusName = rcv.getSender().getLocalName();
					String busLine =  rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
					stationStrategy.addBusToFarAwayQueue(agentBusName,
                               getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME), 
                               getInstantParameter(rcv,MessageParameter.SCHEDULE_ARRIVAL ));
					stationStrategy.addMappingOfBusAndTheirAgent(agentBusName,busLine);
					Print("Got INFORM from " + rcv.getSender().getLocalName());
                    // TO DO: SEND MESSAGE ABOUT PASSENGERS  
				} else if (rcv.getPerformative() == ACLMessage.REQUEST_WHEN) {
					
					
					stationStrategy.removeBusFromFarAwayQueue(rcv.getSender().getLocalName());
					stationStrategy.addBusToQueue(rcv.getSender().getLocalName(),
							getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME),
							getInstantParameter(rcv,MessageParameter.SCHEDULE_ARRIVAL ));
					ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
					msg.addReceiver(rcv.getSender());


					Print("Got REQUEST_WHEN from " + rcv.getSender().getLocalName());
					send(msg);
				}
				else if (rcv.getPerformative() == ACLMessage.AGREE) {
					stationStrategy.removeBusFromBusOnStationQueue(rcv.getSender().getLocalName());
					Print("Got AGREE from " + rcv.getSender().getLocalName());
                  
				}
				else {
					System.out.println("SMTH WRONG");
				}
				
			}
			private void handleMessageFromPedestrian(ACLMessage rcv) {
				if (rcv.getPerformative() == ACLMessage.INFORM) {
					// TODO Handle inform from Pedestrian (has anticipated time of arrival)
					stationStrategy.addPedestrianToFarAwayQueue(rcv.getSender().getLocalName(),
							rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS),
							getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
				}
				
				else if (rcv.getPerformative() == ACLMessage.REQUEST_WHEN) {
					// TODO Handle request_when from Pedestrian (arrived and awaits request for departure)
					//  use MessageParameter.STATION_ID
					System.out.println("GET MESSAGE FROM PEDESTIAN REQUEST_WHEN");
					stationStrategy.removePedestrianFromFarAwayQueue(rcv.getSender().getLocalName());
					stationStrategy.addPedestrianToQueue(rcv.getSender().getLocalName(),
							rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS),
							getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
					ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
					msg.addReceiver(rcv.getSender());
					send(msg);
				}
				else if (rcv.getPerformative() == ACLMessage.AGREE) {
					System.out.println("-----GET AGREE from BUS------");
					stationStrategy.removePedestrianFromBusOnStationQueue(rcv.getSender().getLocalName());
				}
				else {
					System.out.println("SMTH WRONG");
				}
				
			}
		};
		 Behaviour checkState = new TickerBehaviour(this, 100) {

	            @Override
	            protected void onTick() {
	               
	                OptimizationResult result = stationStrategy.requestBusesAndPeopleFreeToGo();
	             
	                handleOptimizationResult(result);
	            }

	            private void handleOptimizationResult(OptimizationResult result) {
	            	 List<Pair<String,List<String>>> elementsFreeToProceed = result.busesAndPedestriansFreeToProceed();
	                for (Pair<String,List<String>> busAndPedestrians : elementsFreeToProceed) {
	                    answerBusCanProceed(busAndPedestrians.getValue0());
	                    answerPedestriansCanProceed(busAndPedestrians.getValue0(),busAndPedestrians.getValue1());
	                }
	            }

	            private void answerPedestriansCanProceed(String busAgentName,List<String> pedestriansAgentsNames) {
					for(String pedestrianAgentName : pedestriansAgentsNames) {
						 ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			                msg.addReceiver(new AID(pedestrianAgentName, AID.ISLOCALNAME));
			                Properties properties = new Properties();
			                properties.setProperty(MessageParameter.TYPE, MessageParameter.STATION);
			                properties.setProperty(MessageParameter.BUS_ID,busAgentName);
			                msg.setAllUserDefinedParameters(properties);
			                send(msg);
					}
					
				}

				private void answerBusCanProceed(String busAgentName) {
					 ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		                msg.addReceiver(new AID(busAgentName, AID.ISLOCALNAME));
		                Properties properties = new Properties();
		                properties.setProperty(MessageParameter.TYPE, MessageParameter.STATION);
		                msg.setAllUserDefinedParameters(properties);
		                send(msg);
					
				}

				

	        };

			addBehaviour(communication);
			addBehaviour(checkState);
	
	}
	
	
	public final long getAgentId() {
		return agentId;
	}
	
	public void takeDown() {
		super.takeDown();
	}

	void Print(String message) {
		System.out.println(getLocalName() + ": " + message);
	}
}
