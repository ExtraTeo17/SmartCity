package GUI;

public class OSMLight extends OSMNode {
	
	public final long adherentOsmWayId;

	public OSMLight(OSMNode node, String adherentOsmWayId) {
		super(node);
		this.adherentOsmWayId = Long.parseLong(adherentOsmWayId);
	}
}
