package SmartCity;

import java.util.List;

final class AgentData {
	private static int ARGS_AMOUNT = 2;
	private static String SEMICOLON = ";";
	
	private List<AgentCreator> agentsToCreate;
	
	public String[] toJadeArgs() {
		String[] args = new String[ARGS_AMOUNT];
		args[0] = "-gui";
		args[1] = getAgentsArgument();
		return args;
	}
	
	private String getAgentsArgument() {
		StringBuilder builder = new StringBuilder();
		for (AgentCreator agent : agentsToCreate) {
			builder.append(agent.toJadeArg() + SEMICOLON);
		}
		return builder.toString();
	}
}
