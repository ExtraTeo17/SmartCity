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
import java.awt.*;
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
    private Router router;
    private HighwayAccessor accessor;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public MapWindow(SmartCityAgent agent) {
        this();
        SmartCityAgent = agent;
    }

    public MapWindow() {
        MapViewer = new JXMapViewer();
        router = new Router();
        accessor = new HighwayAccessor();
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
                System.out.println("In MapViewer: X: " + point.getX() + " Y:" + point.getY());
                GeoPosition pos2 = MapViewer.convertPointToGeoPosition(point);
                System.out.println("After conversion: Lat: " + pos2.getLatitude() + " Lon: " + pos2.getLongitude());
                
                router.addPoint(MapViewer, pos2);
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout(0, 0));
        MainPanel.setMinimumSize(new Dimension(800, 600));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(600);
        splitPane1.setDividerSize(10);
        MainPanel.add(splitPane1, BorderLayout.CENTER);
        MapPanel = new JPanel();
        MapPanel.setLayout(new BorderLayout(0, 0));
        MapPanel.setMinimumSize(new Dimension(-1, -1));
        splitPane1.setLeftComponent(MapPanel);
        SidePanel = new JPanel();
        SidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        splitPane1.setRightComponent(SidePanel);
        addTestCarButton = new JButton();
        addTestCarButton.setText("Add test car");
        addTestCarButton.putClientProperty("hideActionText", Boolean.FALSE);
        SidePanel.add(addTestCarButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return MainPanel;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout(0, 0));
        MainPanel.setMinimumSize(new Dimension(800, 600));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(600);
        splitPane1.setDividerSize(10);
        MainPanel.add(splitPane1, BorderLayout.CENTER);
        MapPanel = new JPanel();
        MapPanel.setLayout(new BorderLayout(0, 0));
        MapPanel.setMinimumSize(new Dimension(-1, -1));
        splitPane1.setLeftComponent(MapPanel);
        SidePanel = new JPanel();
        SidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        splitPane1.setRightComponent(SidePanel);
        addTestCarButton = new JButton();
        addTestCarButton.setText("Add test car");
        addTestCarButton.putClientProperty("hideActionText", Boolean.FALSE);
        SidePanel.add(addTestCarButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return MainPanel;
    }
}