package agents;

import java.util.HashSet;

import com.google.common.eventbus.EventBus;

import agents.abstractions.AbstractAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import osmproxy.buses.BusInfo;
import smartcity.ITimeProvider;

public class BusManagerAgent extends AbstractAgent {

	private static final String NAME = "BusManager";
	
	private HashSet<BusInfo> busInfos;

	public BusManagerAgent(ITimeProvider timeProvider, EventBus eventBus, HashSet<BusInfo> busInfos) {
		super(1, NAME, timeProvider, eventBus);
		this.busInfos = busInfos;
	}

	@Override
	protected void setup() {
		super.setup();
		
		Behaviour communication = new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage rcv = receive();
				if (rcv != null) {
					switch (rcv.getPerformative()) {
					case ACLMessage.INFORM:
						handleRouteQuery(rcv);
						break;
					}
				}
			}
			
			private void handleRouteQuery(ACLMessage rcv) {
				long stationOsmIdFrom = rcv.getUserDefinedParameter(MessageParameter.STATION_FROM_ID);
				long stationOsmIdTo = rcv.getUserDefinedParameter(MessageParameter.STATION_TO_ID);
				AC
			}
			
		};
		
		addBehaviour(communication);
	}

	@Override
	protected void takeDown() {
		// TODO Auto-generated method stub
		super.takeDown();
	}
}
