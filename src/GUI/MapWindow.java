package GUI;

import Agents.TestCar;
import SmartCity.SmartCityAgent;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
