package web;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void setZone(List<GeoPosition> positions);
}
