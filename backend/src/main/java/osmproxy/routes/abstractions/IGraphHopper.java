package osmproxy.routes;

import gnu.trove.set.TIntSet;

import java.util.Collection;

public interface IGraphHopper {
    // TODO: Utilize edge functions in the trouble generating strategy
    void addForbiddenEdges(Collection<Integer> edgeIds);

    void removeForbiddenEdges(Collection<Integer> edgeIds);

    TIntSet getForbiddenEdges();

    long getOSMWay(int internalEdgeId);
}
