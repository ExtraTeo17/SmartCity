package SmartCity;

final class AgentCandidate {
	private static String COLON = ":";
	private static String DOT = ".";
	private static String COMMA = ",";
	private static String LEFT_BRACKET = "(";
	private static String RIGHT_BRACKET = ")";
	private static String PACKAGE = "Agents";
	
	private AgentType agentType;
	private int xPos, yPos;
	private int id;
	
	public AgentCandidate(AgentType agentType, int xPos, int yPos, int id) {
		this.agentType = agentType;
		this.xPos = xPos;
		this.yPos = yPos;
		this.id = id;
	}
	
	public String toJadeArg() {
		return getID() + COLON + PACKAGE + DOT + getAgentType() + getAgentArgs();
	}
	
	private String getID() {
		return agentType.toString() + id;
	}
	
	private String getAgentType() {
		return agentType.toString();
	}
	
	private String getAgentArgs() {
		return LEFT_BRACKET + xPos + COMMA + yPos + RIGHT_BRACKET;
	}
}
