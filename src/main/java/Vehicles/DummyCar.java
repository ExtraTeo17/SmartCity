package Vehicles;

import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

public class DummyCar extends Vehicle {

    @Override
    public String getVehicleType() {
        return null;
    }

    @Override
    public void CalculateRoute() {

    }

    
    @Override
    public String getPositionString() {
        return null;
    }

    @Override
    public GeoPosition getPosition() {
        return null;
    }

    @Override
    public String getCurrentTrafficLightID() {
        return "";
    }

    @Override
    public boolean isAtTrafficLights() {
        return false;
    }

    @Override
    public boolean isAtDestination() {
        return false;
    }

    @Override
    public void Move() {

    }

    @Override
    public List<RouteNode> getFullRoute() {
        return null;
    }

    @Override
    public boolean isAllowedToPass() {
        return false;
    }

    @Override
    public void setAllowedToPass(boolean value) {

    }

	@Override
	public long getAdjacentOsmWayId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LightManagerNode findNextTrafficLight() {
		// TODO Auto-generated method stub
		return null;
	}
}
