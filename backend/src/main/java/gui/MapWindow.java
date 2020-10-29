package gui;

import com.google.inject.Inject;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.MapClickListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.ConfigContainer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.*;

@Deprecated
public class MapWindow {
    private static final Logger logger = LoggerFactory.getLogger(MapWindow.class);
    private static final int REFRESH_MAP_INTERVAL_MILLISECONDS = 100;
    private final ConfigContainer configContainer;

    public JPanel MainPanel;
    private final JXMapViewer MapViewer;
    private boolean renderPedestrians = false;
    private boolean renderPedestrianRoutes = false;
    private boolean renderCars = false;
    private boolean renderCarRoutes = false;
    private boolean renderBuses = false;
    private boolean renderBusRoutes = false;
    private boolean renderZone = false;
    private boolean renderLights = false;
    private boolean renderStations = false;
    private JPanel MapPanel;
    private JPanel SidePanel;
    private JButton StartRouteButton;
    private JSpinner radiusSpinner;
    private JSpinner carLimitSpinner;
    private JSpinner seedSpinner;
    private JSpinner latSpinner;
    private JSpinner lonSpinner;
    private JButton setZoneButton;
    private JSpinner testCarIdSpinner;
    private JSpinner setTimeSpinner;
    private JLabel currentTimeLabel;
    private JLabel currentTimeTitle;
    private JLabel ResultTimeLabel;
    private JCheckBox UseStrategyCheckBox;
    private JLabel ResultTimeTitle;
    private JButton testBusZoneButton;
    private JButton testCarZoneButton;
    private final Timer refreshTimer = new Timer(true);
    private final Random random = new Random();
    private static final DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss dd-MM-yyyy");

    @Inject
    public MapWindow(ConfigContainer configContainer) {
        this.configContainer = configContainer;

        MapViewer = new JXMapViewer();
        currentTimeLabel.setVisible(false);
        currentTimeTitle.setVisible(false);
        ResultTimeLabel.setVisible(false);
        ResultTimeTitle.setVisible(false);
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        MapViewer.setTileFactory(tileFactory);
        GeoPosition warsaw = new GeoPosition(52.24, 21.02);
        MapViewer.setZoom(7);
        MapViewer.setAddressLocation(warsaw);
        radiusSpinner.setModel(new SpinnerNumberModel(600, 100, 50000, 100));
        carLimitSpinner.setModel(new SpinnerNumberModel(4, 1, 1000, 1));
        carLimitSpinner.addChangeListener(e -> {
            int value = (int) carLimitSpinner.getValue();
            if (getTestCarId() > value) {
                testCarIdSpinner.setValue(value);
            }
        });


        seedSpinner.setModel(new SpinnerNumberModel(69, 0, 999999, 1));
        latSpinner.setModel(new SpinnerNumberModel(52.23682, -90, 90, 1));
        lonSpinner.setModel(new SpinnerNumberModel(21.01681, -180, 180, 0.001));

        testCarIdSpinner.setModel(new SpinnerNumberModel(2, 1, 100, 1));
        testCarIdSpinner.addChangeListener(e -> {
            int value = (int) testCarIdSpinner.getValue();
            int limit = getCarLimit();
            if (value > limit) {
                value = limit;
                testCarIdSpinner.setValue(value);
            }
        });

        testCarZoneButton.addActionListener(e -> {
            latSpinner.setValue(52.23682);
            lonSpinner.setValue(21.01681);
            seedSpinner.setValue(34);
            radiusSpinner.setValue(600);
        });

        testBusZoneButton.addActionListener(e -> {
            latSpinner.setValue(52.237037);
            lonSpinner.setValue(21.017928);
            radiusSpinner.setValue(190);

        });

        setTimeSpinner.setModel(new SpinnerDateModel());
        JSpinner.DateEditor dateTimeEditor = new JSpinner.DateEditor(setTimeSpinner, "HH:mm:ss dd-MM-yyyy");
        setTimeSpinner.setEditor(dateTimeEditor);
        var date = new Date(2020, 10, 14, 12, 25);
        setTimeSpinner.setValue(date);

        //Left click event
        MapViewer.addMouseListener(new MapClickListener(MapViewer) {
            @Override
            public void mapClicked(GeoPosition geoPosition) {

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
    }

    public void display() {
        var mapViewer = this.MapViewer;
        JFrame frame = new JFrame("Smart City by Katherine & Przemyslaw & Robert");
        frame.getContentPane().add(this.MainPanel);
        JMenuBar menuBar = new JMenuBar();
        JMenu view = new JMenu("View");

        final JCheckBoxMenuItem cars = new JCheckBoxMenuItem("Render cars", this.renderCars);
        var window = this;
        cars.addItemListener(e -> window.renderCars = cars.getState());
        view.add(cars);

        final JCheckBoxMenuItem routes = new JCheckBoxMenuItem("Render car routes", this.renderCarRoutes);
        routes.addItemListener(e -> window.renderCarRoutes = routes.getState());
        view.add(routes);

        final JCheckBoxMenuItem buses = new JCheckBoxMenuItem("Render buses", this.renderBuses);
        buses.addItemListener(e -> window.renderBuses = buses.getState());
        view.add(buses);

        final JCheckBoxMenuItem busRoutes = new JCheckBoxMenuItem("Render bus routes", this.renderBusRoutes);
        busRoutes.addItemListener(e -> window.renderBusRoutes = busRoutes.getState());
        view.add(busRoutes);

        final JCheckBoxMenuItem pedestrian = new JCheckBoxMenuItem("Render pedestrians", this.renderPedestrians);
        pedestrian.addItemListener(e -> window.renderPedestrians = pedestrian.getState());
        view.add(pedestrian);

        final JCheckBoxMenuItem pedestrianRoutes = new JCheckBoxMenuItem("Render pedestrian routes", this.renderPedestrianRoutes);
        pedestrianRoutes.addItemListener(e -> window.renderPedestrianRoutes = pedestrianRoutes.getState());
        view.add(pedestrianRoutes);

        final JCheckBoxMenuItem lights = new JCheckBoxMenuItem("Render lights", this.renderLights);
        lights.addItemListener(e -> window.renderLights = lights.getState());
        view.add(lights);

        final JCheckBoxMenuItem zone = new JCheckBoxMenuItem("Render zone", this.renderZone);
        zone.addItemListener(e -> window.renderZone = zone.getState());
        view.add(zone);

        final JCheckBoxMenuItem stations = new JCheckBoxMenuItem("Render stations", this.renderStations);
        stations.addItemListener(e -> window.renderStations = stations.getState());
        view.add(stations);

        menuBar.add(view);

        JMenu debug = new JMenu("Debug");

        JMenuItem runTest = new JMenuItem("Test crossroad");
        runTest.addActionListener(e -> {
            window.setInputEnabled(false);
            double lat = 52.23702507833161;
            double lng = 21.017934679985046;
            mapViewer.setAddressLocation(new GeoPosition(lat, lng));
            mapViewer.setZoom(1);

        });
        debug.add(runTest);
        menuBar.add(debug);
        this.addGenerationMenu(menuBar);

        frame.setJMenuBar(menuBar);
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private void addGenerationMenu(JMenuBar menuBar) {
        JMenu generation = new JMenu("Generation");

        final JCheckBoxMenuItem car_gen = new JCheckBoxMenuItem("Cars");
        generation.add(car_gen);


        menuBar.add(generation);
    }

    public void setResultTime(String val) {
        ResultTimeLabel.setText(val);
    }

    private void setInputEnabled(boolean check) {
        carLimitSpinner.setEnabled(check);
        seedSpinner.setEnabled(check);
        radiusSpinner.setEnabled(check);
        latSpinner.setEnabled(check);
        lonSpinner.setEnabled(check);
        testCarIdSpinner.setEnabled(check);
        setZoneButton.setEnabled(check);
        StartRouteButton.setEnabled(check);
        setTimeSpinner.setEnabled(check);
        testCarZoneButton.setEnabled(check);
        testBusZoneButton.setEnabled(check);
    }

    private int getCarLimit() {
        return 8;
    }

    private int getTestCarId() {
        return 4;
    }

    private int getSeed() {
        return (int) seedSpinner.getValue();
    }

    private void refreshTime() {
    }


    private void drawVehicles(List<Painter<JXMapViewer>> painters) {
    }

    private void drawRoutes(List<Painter<JXMapViewer>> painters) {
    }

    private void drawPedestrians(List<Painter<JXMapViewer>> painters) {
    }

    private void drawPedestrianRoutes(List<Painter<JXMapViewer>> painters) {
    }

    private void drawBuses(List<Painter<JXMapViewer>> painters) {
    }

    private void drawBusRoutes(List<Painter<JXMapViewer>> painters) {
    }

    private void drawZones(Collection<Painter<JXMapViewer>> painters) {

    }

    private void drawStations(List<Painter<JXMapViewer>> painters) {
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
        splitPane1.setDividerLocation(565);
        splitPane1.setDividerSize(10);
        MainPanel.add(splitPane1, BorderLayout.CENTER);
        MapPanel = new JPanel();
        MapPanel.setLayout(new BorderLayout(0, 0));
        MapPanel.setMinimumSize(new Dimension(-1, -1));
        splitPane1.setLeftComponent(MapPanel);
        SidePanel = new JPanel();
        SidePanel.setLayout(new GridBagLayout());
        SidePanel.setMaximumSize(new Dimension(300, 32767));
        SidePanel.setMinimumSize(new Dimension(300, 40));
        SidePanel.setPreferredSize(new Dimension(300, 40));
        splitPane1.setRightComponent(SidePanel);
        SidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer1, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Car limit");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label1, gbc);
        carLimitSpinner = new JSpinner();
        carLimitSpinner.setMaximumSize(new Dimension(150, 30));
        carLimitSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(carLimitSpinner, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Simulation seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label2, gbc);
        seedSpinner = new JSpinner();
        seedSpinner.setMaximumSize(new Dimension(150, 30));
        seedSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(seedSpinner, gbc);
        StartRouteButton = new JButton();
        StartRouteButton.setText("Start vehicles");
        StartRouteButton.putClientProperty("hideActionText", Boolean.FALSE);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 15;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(StartRouteButton, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Zone center:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label3, gbc);
        latSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(latSpinner, gbc);
        lonSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(lonSpinner, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Latitude");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Longitude");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Zone radius (in m)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label6, gbc);
        radiusSpinner = new JSpinner();
        radiusSpinner.setMaximumSize(new Dimension(150, 30));
        radiusSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(radiusSpinner, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer2, gbc);
        setZoneButton = new JButton();
        setZoneButton.setText("Set zone");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SidePanel.add(setZoneButton, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Test car ID");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label7, gbc);
        testCarIdSpinner = new JSpinner();
        testCarIdSpinner.setMaximumSize(new Dimension(150, 30));
        testCarIdSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(testCarIdSpinner, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Simulation time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(label8, gbc);
        setTimeSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SidePanel.add(setTimeSpinner, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer3, gbc);
        currentTimeLabel = new JLabel();
        currentTimeLabel.setHorizontalAlignment(0);
        currentTimeLabel.setHorizontalTextPosition(0);
        currentTimeLabel.setText("...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 16;
        SidePanel.add(currentTimeLabel, gbc);
        currentTimeTitle = new JLabel();
        currentTimeTitle.setText("Current time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(currentTimeTitle, gbc);
        ResultTimeTitle = new JLabel();
        ResultTimeTitle.setText("Result");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(ResultTimeTitle, gbc);
        ResultTimeLabel = new JLabel();
        ResultTimeLabel.setHorizontalAlignment(0);
        ResultTimeLabel.setHorizontalTextPosition(0);
        ResultTimeLabel.setText("...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 17;
        SidePanel.add(ResultTimeLabel, gbc);
        UseStrategyCheckBox = new JCheckBox();
        UseStrategyCheckBox.setSelected(true);
        UseStrategyCheckBox.setText("Use LightStrategy");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(UseStrategyCheckBox, gbc);
        testBusZoneButton = new JButton();
        testBusZoneButton.setText("Test Bus zone");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SidePanel.add(testBusZoneButton, gbc);
        testCarZoneButton = new JButton();
        testCarZoneButton.setText("Test Car zone");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SidePanel.add(testCarZoneButton, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer4, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return MainPanel; }

    public class RefreshTask extends TimerTask { // OCB ?????? TOO OFTEN
        @Override
        public void run() {
            try {

            } catch (Exception e) {
                logger.error("Error refreshing simulation", e);
            }
        }
    }
}
