package GUI;

import Agents.TestCar;
import SmartCity.SmartCityAgent;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.*;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

public class MapWindow {
    public JPanel MainPanel;
    public JXMapViewer MapViewer;
    private JPanel MapPanel;
    private JPanel SidePanel;
    private JButton addTestCarButton;
    private SmartCityAgent SmartCityAgent;

    public MapWindow() {
        MapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        MapViewer.setTileFactory(tileFactory);
        GeoPosition warsaw = new GeoPosition(52.24, 21.02);
        MapViewer.setZoom(7);
        MapViewer.setAddressLocation(warsaw);

        //Left click event
        MapViewer.addMouseListener(new MapClickListener(MapViewer) {
            @Override
            public void mapClicked(GeoPosition geoPosition) {
                System.out.println("Clicked.");
                System.out.println("Lat: " + geoPosition.getLatitude() + " Lon: " + geoPosition.getLongitude());
                Point2D point = MapViewer.convertGeoPositionToPoint(geoPosition);
                System.out.println("In MapViewer: X: " + point.getX()+ " Y:" + point.getY());
                GeoPosition pos2 = MapViewer.convertPointToGeoPosition(point);
                System.out.println("After conversion: Lat: " + pos2.getLatitude() + " Lon: " + pos2.getLongitude());
            }
        });

        //Pan on right click + drag
        MouseInputListener mia = new PanRightMouseInputListener(MapViewer);
        MapViewer.addMouseListener(mia);
        MapViewer.addMouseMotionListener(mia);

        //Zoom on scroll wheel
        MapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(MapViewer));

        MapPanel.add(MapViewer);
        MapPanel.revalidate();
        addTestCarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SmartCityAgent.AddNewAgent("Test" + SmartCityAgent.AgentCount, new TestCar());
                } catch (StaleProxyException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public MapWindow(SmartCityAgent agent) {
        this();
        SmartCityAgent = agent;
    }
}
