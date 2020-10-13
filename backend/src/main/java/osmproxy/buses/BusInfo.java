package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BusInfo implements Iterable<BrigadeInfo> {
    public final String busLine;
    public final List<OSMWay> route;
    public final List<OSMStation> stops;

    private final List<BrigadeInfo> brigadeList;

    BusInfo(String busLine, List<OSMWay> route) {
        this.busLine = busLine;
        this.route = route;
        this.stops = new ArrayList<>();
        this.brigadeList = new ArrayList<>();
    }

    void addStops(Collection<OSMStation> stops) {
        this.stops.addAll(stops);
    }

    void addBrigades(Collection<BrigadeInfo> brigades) {
        brigadeList.addAll(brigades);
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }
}
