package osmproxy;

import com.graphhopper.routing.weighting.AvoidEdgesWeighting;
import com.graphhopper.routing.weighting.Weighting;
import gnu.trove.set.TIntSet;

import java.util.Collection;

public class AvoidEdgesRemovableWeighting extends AvoidEdgesWeighting {

    public static final String NAME = "avoid_edges_removable";
    public static final int NULL_INT = 0;
    public static final double NULL_DOUBLE = 0;


    public AvoidEdgesRemovableWeighting(Weighting superWeighting) {
        super(superWeighting);
    }

    /**
     * This method removes the specified path to this weighting which should be penalized in the
     * calcWeight method.
     */
    public void removeEdgeIds(final Collection<Integer> edgeIds) {
        for (int edgeId : edgeIds) {
            visitedEdges.remove(edgeId);
        }
    }

    public void addEdgeIds(final Collection<Integer> edgeIds) {
        for (int edgeId : edgeIds) {
            visitedEdges.add(edgeId);
        }
    }

    public final TIntSet getEdgeIds() {
        return visitedEdges;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
