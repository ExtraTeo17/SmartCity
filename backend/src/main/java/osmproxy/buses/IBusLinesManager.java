package osmproxy.buses;


import routing.RouteNode;
import smartcity.buses.Timetable;

import java.util.List;

public interface IBusLinesManager {
    @FunctionalInterface
    public interface CreateBusFunc<T, U, V, W, R> {
        public T apply(U u, V v, W w, R r);
    }

    boolean prepareStationsAndBuses(CreateBusFunc<Boolean, Timetable, List<RouteNode>,
            String, String> createBusFunc);
}
