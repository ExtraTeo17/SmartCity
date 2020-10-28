package smartcity.lights.abstractions;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import smartcity.lights.OptimizationResult;
import smartcity.lights.core.Light;
import smartcity.stations.ArrivalInfo;

import java.util.List;

// TODO: Interface is too big and too specific, make it more general and move some methods to different interface
@SuppressWarnings("UnusedReturnValue")
public interface ICrossroad {
    List<Light> getLights();

    boolean addCarToQueue(long adjacentWayId, String agentName);

    boolean removeCarFromQueue(long adjacentWayId);

    boolean addCarToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo);

    boolean removeCarFromFarAwayQueue(long adjacentWayId, String agentName);

    boolean addPedestrianToQueue(long adjacentWayId, String agentName);

    boolean removePedestrianFromQueue(long adjacentWayId);

    boolean addPedestrianToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo);

    boolean removePedestrianFromFarAwayQueue(long adjacentWayId, String agentName);

    OptimizationResult requestOptimizations(int extendTimeSeconds);

    void draw(List<Painter<JXMapViewer>> painter);

    void startLifetime();
}
