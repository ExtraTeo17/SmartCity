package osmproxy.routes;

import com.graphhopper.routing.weighting.AvoidEdgesWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collection;

public class AvoidEdgesRemovableWeighting extends AvoidEdgesWeighting {

    public static final String NAME = "avoid_edges_removable";
    public static final int NULL_INT = 0;
    public static final double NULL_DOUBLE = 0;


    AvoidEdgesRemovableWeighting(Weighting superWeighting) {
        super(superWeighting);
    }

    /**
     * This method removes the specified path to this weighting which should be penalized in the
     * calcWeight method.
     */
    void removeEdgeIds(final Collection<Integer> edgeIds) {
        for (int edgeId : edgeIds) {
            visitedEdges.remove(edgeId);
        }
    }


    void addEdgeIds(final Collection<Integer> edgeIds) {
        for (int edgeId : edgeIds) {
            visitedEdges.add(edgeId);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    //TODO: Docs
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        double weight = superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
        if (visitedEdges.contains(edgeState.getEdge())) {
            return Double.MAX_VALUE;
        }

        return weight;
    }
}
