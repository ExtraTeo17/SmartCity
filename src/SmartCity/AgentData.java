package SmartCity;

import java.util.ArrayList;
import java.util.List;

final class AgentData {
	private static int ARGS_AMOUNT = 2;
	private static String SEMICOLON = ";";
	
	private List<AgentCandidate> agentsToCreate;
	
	public AgentData() {
		agentsToCreate = new ArrayList<>();
	}
	
	public void Add(AgentCandidate candidate) {
		agentsToCreate.add(candidate);
	}
	
	public String[] toJadeArgs() {
		String[] args = new String[ARGS_AMOUNT];
		args[0] = "-gui";
		args[1] = getAgentsArgument();
		return args;
	}
	
	private String getAgentsArgument() {
		StringBuilder builder = new StringBuilder();
		for (AgentCandidate agent : agentsToCreate) {
			builder.append(agent.toJadeArg() + SEMICOLON);
		}
		return builder.toString();
	}
}
