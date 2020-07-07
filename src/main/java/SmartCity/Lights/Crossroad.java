package SmartCity.Lights;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.time.Instant;
import java.util.List;

public abstract class Crossroad {

    public static boolean STRATEGY_ACTIVE = true;

    public abstract void addCarToQueue(String carName, long adjacentOsmWayId);

    public abstract void addCarToFarAwayQueue(String carName, long adjacentOsmWayId, Instant journeyTime);

    public abstract void removeCarFromQueue(long adjacentOsmWayId);

    public abstract void removeCarFromFarAwayQueue(String carName, long adjacentOsmWayId);

    public abstract void addPedestrianToQueue(String pedestrianName, long adjacentOsmWayId);

    public abstract void addPedestrianToFarAwayQueue(String pedestrianName, long adjacentOsmWayId, Instant journeyTime);

    public abstract void removePedestrianFromQueue(long adjacentOsmWayId);

    public abstract void removePedestrianFromFarAwayQueue(String pedestrianName, long adjacentOsmWayId);

    public abstract OptimizationResult requestOptimizations();

    public abstract boolean isLightGreen(long adjacentOsmWayId);

    public abstract void draw(List<Painter<JXMapViewer>> painter);

    public abstract void startLifetime();
}
