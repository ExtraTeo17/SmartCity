package osmproxy.routes.abstractions;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;

import java.util.Collection;
import java.util.List;

public interface IGraphHopper {
    void addForbiddenEdges(Collection<Integer> edgeIds);

    void removeForbiddenEdges(Collection<Integer> edgeIds);

    List<Path> calcPaths(GHRequest request, GHResponse ghRsp);

    long getOSMWay(int internalEdgeId);
}
