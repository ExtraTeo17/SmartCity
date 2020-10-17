package osmproxy;

import java.util.Collection;

import com.graphhopper.routing.weighting.AvoidEdgesWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

import gnu.trove.set.TIntSet;

public class AvoidEdgesRemovableWeighting extends AvoidEdgesWeighting {

	public static final String NAME = "avoid_edges_removable";

	public AvoidEdgesRemovableWeighting(Weighting superWeighting) {
		super(superWeighting);
	}

    /**
     * This method removes the specified path to this weighting which should be penalized in the
     * calcWeight method.
     */
    public void removeEdges(Collection<EdgeIteratorState> edges) {
        for (EdgeIteratorState edge : edges) {
            visitedEdges.remove(edge.getEdge());
        }
    }

    public final TIntSet getEdges() {
    	return visitedEdges;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
