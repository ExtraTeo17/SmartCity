package SmartCity.Stations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import OSMProxy.Elements.OSMStation;
import Routing.StationNode;
import SmartCity.SmartCityAgent;
import SmartCity.Lights.Light;
import SmartCity.Lights.OptimizationResult;

public class StationStrategy {
	//AgentName - Schedule Arrival Time / Arrival Time
	final private Map<String, Pair<Instant,Instant>> farAwayBusMap = new HashMap<>();
	final private Map<String, Pair<Instant,Instant>> busOnStationMap = new HashMap<>();
	final private Map<String, String> busAgentNameToBusNumberMap = new HashMap<>();
	final private Map<String,PedestrianArrivalInfo> farAwayPedestrianMap = new HashMap<>();
	final private Map<String, PedestrianArrivalInfo> pedestrianOnStationMap = new HashMap<>();
	final private static int MINUTE = 60;
	//private final OSMStation stationOSMNode;
	public StationStrategy(OSMStation stationOSMNode, long agentId) {
		SmartCityAgent.osmStationIdToStationNode.put(stationOSMNode.getId(), new StationNode(stationOSMNode.getLat(),
				stationOSMNode.getLon(), Long.toString(stationOSMNode.getId()), agentId));
	}
	
	  public void addBusToFarAwayQueue(String agentBusName, Instant arrivalTime,Instant scheduleArrivalTime) {
		  farAwayBusMap.put(agentBusName, Pair.with(scheduleArrivalTime, arrivalTime));
	    }
	  public void addMappingOfBusAndTheirAgent(String agentBusName,String busNumber ) {
		  busAgentNameToBusNumberMap.put(agentBusName,busNumber );
	    }
	  public void addBusToQueue(String agentBusName, Instant arrivalTime,Instant scheduleArrivalTime) {
		  busOnStationMap.put(agentBusName, Pair.with(scheduleArrivalTime, arrivalTime));
	    }
	  public void removeBusFromFarAwayQueue(String agentName) {
		  farAwayBusMap.remove(agentName);
	    }
	  public void removeBusFromBusOnStationQueue(String agentName) {
		  busOnStationMap.remove(agentName);
	    }
	  
	  
	  
	  public void addPedestrianToFarAwayQueue(String agentName,String desiredBus, Instant arrivalTime) {
		  if(!farAwayPedestrianMap.containsKey(desiredBus)) {

			  farAwayPedestrianMap.put(desiredBus, new PedestrianArrivalInfo());
		  }
		  farAwayPedestrianMap.get(desiredBus).putPedestrianOnList( new Pair<String,Instant>(agentName,arrivalTime));
	    }
	  public void addPedestrianToQueue(String agentName, String desiredBus, Instant arrivalTime) {
		  if(!pedestrianOnStationMap.containsKey(desiredBus)) {

			  pedestrianOnStationMap.put(desiredBus, new PedestrianArrivalInfo());
		  }
		  pedestrianOnStationMap.get(desiredBus).putPedestrianOnList( new Pair<String,Instant>(agentName,arrivalTime));
		
	    }
	  //TODO : CHANGE DOESN@T MATCH THE LOGIC
	  public void removePedestrianFromFarAwayQueue(String agentName) {
		  farAwayPedestrianMap.remove(agentName);
	    }
	  public void removePedestrianFromBusOnStationQueue(String agentName) {
		  pedestrianOnStationMap.remove(agentName);
	    }


	public OptimizationResult requestBusesAndPeopleFreeToGo() {
		  OptimizationResult result = new OptimizationResult();
		for(String bus : busOnStationMap.keySet() )
	      {
	    	  Pair<Instant,Instant> scheduleAndArrivalTime = busOnStationMap.get(bus);
	    	  if(scheduleAndArrivalTime.getValue1().isAfter(scheduleAndArrivalTime.getValue0().plusSeconds(MINUTE)))
	    	  {
	    		  
	    		  System.out.println("------------------BUS WAS LATE-----------------------");
	    		  List<String> passengersThatCanLeave =  checkPassengersWhoAreReadyToGo(bus);
	    		  result.addBusAndPedestrianGrantedPassthrough(bus,passengersThatCanLeave);
	    	  }
	    	  else if (scheduleAndArrivalTime.getValue1().isAfter((scheduleAndArrivalTime.getValue0().minusSeconds(MINUTE))) &&
	    			  scheduleAndArrivalTime.getValue1().isBefore((scheduleAndArrivalTime.getValue0().plusSeconds(MINUTE)))) 
	    	  {
	    		  System.out.println("------------------BUS WAS ON TIME-----------------------");
	    		  List<String> passengersThatCanLeave =  checkPassengersWhoAreReadyToGo(bus);
	    		  passengersThatCanLeave.addAll(checkPassengersWhoAreFar(bus, scheduleAndArrivalTime.getValue0().plusSeconds(MINUTE)));
	    		  result.addBusAndPedestrianGrantedPassthrough(bus,passengersThatCanLeave);
	    	  
	    	  }
	    	  else if (scheduleAndArrivalTime.getValue1().isBefore((scheduleAndArrivalTime.getValue0().minusSeconds(MINUTE)))) {
	    		  System.out.println("------------------BUS TOO EARLY-----------------------");
	    		  continue;
	    	  }
	    	  else {
	    		  System.out.println("------------------SOME SHIT HAPPENED-----------------------");
	    	  }
	      }
		return result;
	}

	private List<String> checkPassengersWhoAreReadyToGo(String busAgentName) {
		String bus = busAgentNameToBusNumberMap.get(busAgentName);
		List<String> passengersThatCanLeave = new ArrayList();
		if(pedestrianOnStationMap.containsKey(bus))
		{
			PedestrianArrivalInfo arrivalTime = pedestrianOnStationMap.get(bus);
		for (Pair<String,Instant> pedestrian : arrivalTime.agentNamesAndArrivalTimes) {
			passengersThatCanLeave.add(pedestrian.getValue0());
			}
		
		return passengersThatCanLeave;
		}
	return new ArrayList<String>();	
	}

	private List<String> checkPassengersWhoAreFar(String busAgentName, Instant deadline) {
		
		String bus = busAgentNameToBusNumberMap.get(busAgentName);
		List<String> passengersThatCanLeave = new ArrayList();
		if(farAwayPedestrianMap.containsKey(bus))
		{
			PedestrianArrivalInfo arrivalTime = farAwayPedestrianMap.get(bus);
			for (Pair<String,Instant> pedestrian : arrivalTime.agentNamesAndArrivalTimes) {
				if(pedestrian.getValue1().isBefore(deadline)) {
					 System.out.println("--------Passenger too wait---------");
					passengersThatCanLeave.add(pedestrian.getValue0());
				}
			}
			return passengersThatCanLeave;
		}
		return new ArrayList<String>();	
	
		}
}
