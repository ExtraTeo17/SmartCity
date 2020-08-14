package osmproxy.buses;

import org.jxmapviewer.viewer.GeoPosition;
import routing.RouteNode;
import smartcity.buses.Timetable;

import java.util.List;
import java.util.Set;

public interface IBusLinesManager {
    @FunctionalInterface
    public interface CreateBusFunc<T, U, V, W, R> {
        public T apply(U u, V v, W w, R r);
    }

    boolean prepareStationsAndBuses(GeoPosition middlePoint, int radius, CreateBusFunc<Boolean, Timetable, List<RouteNode>,
            String,String> createBusFunc);
}
