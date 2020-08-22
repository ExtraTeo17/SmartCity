package smartcity.lights;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import routing.core.IGeoPosition;

import java.time.Instant;
import java.util.List;

// TODO: Interface is too big and too specific, make it more general and move some methods to different interface
public interface ICrossroad {
    List<IGeoPosition> getLightsPositions();

    void addCarToQueue(String carName, long adjacentOsmWayId);

    void addCarToFarAwayQueue(String carName, long adjacentOsmWayId, Instant journeyTime);

    void removeCarFromQueue(long adjacentOsmWayId);

    void removeCarFromFarAwayQueue(String carName, long adjacentOsmWayId);

    void addPedestrianToQueue(String pedestrianName, long adjacentOsmWayId);

    void addPedestrianToFarAwayQueue(String pedestrianName, long adjacentOsmWayId, Instant journeyTime);

    void removePedestrianFromQueue(long adjacentOsmWayId);

    void removePedestrianFromFarAwayQueue(String pedestrianName, long adjacentOsmWayId);

    OptimizationResult requestOptimizations();

    void draw(List<Painter<JXMapViewer>> painter);

    void startLifetime();
}
