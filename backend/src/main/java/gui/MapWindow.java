package gui;

import agents.*;
import agents.abstractions.IAgentsContainer;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.SetZoneEvent;
import events.SimulationReadyEvent;
import org.javatuples.Pair;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.MapClickListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMStation;
import routing.*;
import smartcity.ConfigContainer;
import smartcity.MasterAgent;
import smartcity.lights.SimpleCrossroad;
import vehicles.Bus;
import vehicles.Pedestrian;
import vehicles.TestCar;
import vehicles.TestPedestrian;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.*;

@Deprecated
public class MapWindow {
    private static final Logger logger = LoggerFactory.getLogger(MapWindow.class);
    private final static int REFRESH_MAP_INTERVAL_MILLISECONDS = 100;
    private final static int BUS_CONTROL_INTERVAL_MILLISECONDS = 2000; //60000
    private final static int CREATE_CAR_INTERVAL_MILLISECONDS = 500;
    private static final long CREATE_PEDESTRIAN_INTERVAL_MILLISECONDS = 100;
    private final static int PEDESTRIAN_STATION_RADIUS = 200;
    private final static int TIME_SCALE = 10;
    private final EventBus eventBus;
    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;

    public JPanel MainPanel;
    private final JXMapViewer MapViewer;
    private boolean renderPedestrians = true;
    private boolean renderPedestrianRoutes = true;
    private boolean renderCars = true;
    private boolean renderCarRoutes = true;
    private boolean renderBuses = true;
    private boolean renderBusRoutes = true;
    private boolean renderZone = true;
    private boolean renderLights = true;
    private boolean renderStations = true;
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
    private MasterAgent masterAgent;
    private Timer refreshTimer = new Timer(true);
    private final Timer spawnTimer = new Timer(true);
    private IGeoPosition pointA;
    private IGeoPosition pointB;

    public void setState(SimulationState state) {
        if (this.state != state) {
            this.state = state;
            if (state == SimulationState.READY_TO_RUN) {
                eventBus.post(new SimulationReadyEvent());
            }
        }
    }

    private SimulationState state = SimulationState.SETTING_ZONE;
    private final Random random = new Random();
    private final Random testCarRandom = new Random(96);
    private final IZone zone;
    private Instant simulationStart;

    @Inject
    public MapWindow(EventBus eventBus,
                     IAgentsContainer agentsContainer,
                     ConfigContainer configContainer) {
        this.eventBus = eventBus;
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.zone = configContainer.getZone();

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
        radiusSpinner.setModel(new SpinnerNumberModel(200, 100, 50000, 100));
        carLimitSpinner.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
        carLimitSpinner.addChangeListener(e -> {
            if (getCarLimit() - 1 < getTestCarId()) {
                testCarIdSpinner.setValue(getCarLimit() - 1);
            }
        });

        UseStrategyCheckBox.addItemListener(e -> SimpleCrossroad.STRATEGY_ACTIVE = UseStrategyCheckBox.isSelected());

        seedSpinner.setModel(new SpinnerNumberModel(69, 0, 999999, 1));
        latSpinner.setModel(new SpinnerNumberModel(52.203342, -90, 90, 1));
        lonSpinner.setModel(new SpinnerNumberModel(20.861213, -180, 180, 0.001));

        testCarIdSpinner.setModel(new SpinnerNumberModel(40, 0, 100, 1));
        testCarIdSpinner.addChangeListener(e -> {
            if (getTestCarId() >= getCarLimit()) {
                testCarIdSpinner.setValue(testCarIdSpinner.getPreviousValue());
            }
        });
        setZoneButton.addActionListener(e -> {
            prepareAgentsAndSetZone((double) latSpinner.getValue(), (double) lonSpinner.getValue(), getZoneRadius());
            setState(SimulationState.READY_TO_RUN);
        });

        testCarZoneButton.addActionListener(e -> {
            latSpinner.setValue(52.23682);
            lonSpinner.setValue(21.01681);
            seedSpinner.setValue(34);
            radiusSpinner.setValue(600);
            prepareAgentsAndSetZone(52.23682, 21.01681, getZoneRadius());
        });

        testBusZoneButton.addActionListener(e -> {
            latSpinner.setValue(52.203342);
            lonSpinner.setValue(20.861213);
            radiusSpinner.setValue(300);
            if (!configContainer.shouldGeneratePedestriansAndBuses()) {
                logger.warn("Pedestrians won't be generated");
            }
            prepareAgentsAndSetZone(52.203342, 20.861213, getZoneRadius());
        });

        setTimeSpinner.setModel(new SpinnerDateModel());
        JSpinner.DateEditor dateTimeEditor = new JSpinner.DateEditor(setTimeSpinner, "HH:mm:ss dd-MM-yyyy");
        setTimeSpinner.setEditor(dateTimeEditor);
        setTimeSpinner.setValue(new Date());

        //Left click event
        MapViewer.addMouseListener(new MapClickListener(MapViewer) {
            @Override
            public void mapClicked(GeoPosition geoPosition) {
                var lat = geoPosition.getLatitude();
                var lng = geoPosition.getLongitude();
                logger.info("Lat: " + lat + " Lon: " + lng);
                switch (state) {
                    case SETTING_ZONE:
                    case READY_TO_RUN:
                        latSpinner.setValue(lat);
                        lonSpinner.setValue(lng);
                        prepareAgentsAndSetZone(lat, lng, getZoneRadius());
                        state = SimulationState.READY_TO_RUN;
                        break;
                    case RUNNING:
                        if (pointA == null) {
                            pointA = Position.of(lat, lng);
                        }
                        else {
                            pointB = Position.of(lat, lng);
                            var vehicle = masterAgent.tryAddNewVehicleAgent(Router.generateRouteInfo(pointA, pointB));
                            vehicle.start();

                            logger.info("Vehicles: " + agentsContainer.size(VehicleAgent.class));
                            logger.info("Lights: " + agentsContainer.size(LightManager.class));
                            pointA = null;
                            pointB = null;
                        }
                        break;
                }
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
        StartRouteButton.addActionListener(e -> {
            if (state != SimulationState.READY_TO_RUN) {
                return;
            }
            setInputEnabled(false);
            currentTimeTitle.setVisible(true);
            currentTimeLabel.setVisible(true);
            ResultTimeLabel.setVisible(true);
            ResultTimeTitle.setVisible(true);
            if (agentsContainer.size(LightManager.class) == 0) {
                logger.warn("Why?");
            }
            masterAgent.activateLightManagerAgents();
            random.setSeed(getSeed());

            if (configContainer.shouldGenerateCars()) {
                spawnTimer.scheduleAtFixedRate(new CreateCarTask(), 0, CREATE_CAR_INTERVAL_MILLISECONDS);
            }
            if (configContainer.shouldGeneratePedestriansAndBuses()) {
                spawnTimer.scheduleAtFixedRate(new CreatePedestrianTask(), 0, CREATE_PEDESTRIAN_INTERVAL_MILLISECONDS);
            }
            simulationStart = getSimulationStartTime().toInstant();
            refreshTimer.scheduleAtFixedRate(new BusControlTask(), 0, BUS_CONTROL_INTERVAL_MILLISECONDS);
            setState(SimulationState.RUNNING);
        });
        refreshTimer.scheduleAtFixedRate(new RefreshTask(), 0, REFRESH_MAP_INTERVAL_MILLISECONDS);
    }

    private int getZoneRadius() {
        return zone.getRadius();
    }

    public void setSmartCityAgent(MasterAgent smartCityAgent) {
        this.masterAgent = smartCityAgent;
    }

    public void display() {
        var mapViewer = this.MapViewer;
        JFrame frame = new JFrame("Smart City by Katherine & Dominic & Robert");
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
            double lon = 21.017934679985046;
            mapViewer.setAddressLocation(new GeoPosition(lat, lon));
            mapViewer.setZoom(1);
            window.prepareAgentsAndSetZone(lat, lon, 100);
            IGeoPosition N = Position.of(52.23758683540269, 21.017720103263855);
            IGeoPosition S = Position.of(52.23627934304847, 21.018092930316925);
            IGeoPosition E = Position.of(52.237225472020704, 21.019399166107178);
            IGeoPosition W = Position.of(52.23678526174392, 21.016663312911987);

            // N to S
            List<RouteNode> NS;
            try {
                NS = Router.generateRouteInfo(N, S);

                for (int i = 0; i < 5; i++) {
                    masterAgent.tryAddNewVehicleAgent(NS);
                }
            } catch (Exception ex) {
                logger.warn("Error adding vehicle", ex);
                return;
            }

            // S to N
            List<RouteNode> SN;
            try {
                SN = Router.generateRouteInfo(S, N);

                for (int i = 0; i < 5; i++) {
                    masterAgent.tryAddNewVehicleAgent(SN);
                }
            } catch (Exception ex) {
                logger.warn("Error adding vehicle", ex);
                return;
            }

            // E to W
            List<RouteNode> EW;
            try {
                EW = Router.generateRouteInfo(E, W);

                for (int i = 0; i < 5; i++) {
                    masterAgent.tryAddNewVehicleAgent(EW);
                }
            } catch (Exception ex) {
                logger.warn("Error adding vehicle", ex);
                return;
            }

            // W to E
            List<RouteNode> WE;
            try {
                WE = Router.generateRouteInfo(W, E);
                for (int i = 0; i < 5; i++) {
                    masterAgent.tryAddNewVehicleAgent(WE);
                }
            } catch (Exception ex) {
                logger.warn("Error adding vehicle", ex);
                return;
            }

            masterAgent.activateLightManagerAgents();

            // start all
            agentsContainer.forEach(VehicleAgent.class, AbstractAgent::start);
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

        final JCheckBoxMenuItem car_gen = new JCheckBoxMenuItem("Cars", configContainer.shouldGenerateCars());
        car_gen.addItemListener(e -> configContainer.setGenerateCars(car_gen.getState()));
        generation.add(car_gen);

        final JCheckBoxMenuItem pedestrians = new JCheckBoxMenuItem("Pedestrians and buses",
                configContainer.shouldGeneratePedestriansAndBuses());
        pedestrians.addItemListener(e -> configContainer.setGeneratePedestriansAndBuses(pedestrians.getState()));
        generation.add(pedestrians);

        menuBar.add(generation);
    }

    // TODO: To be moved into MasterAgent
    @Subscribe
    public void handleSetZone(SetZoneEvent e) {
        logger.info("Set zone event occurred: " + e.toString());
        if (state == SimulationState.READY_TO_RUN) {
            masterAgent.reset();
            state = SimulationState.SETTING_ZONE;
        }
        prepareAgentsAndSetZone(e.getLat(), e.getLng(), (int) e.getRadius());
        setState(SimulationState.READY_TO_RUN);
    }

    public static int getTimeScale() {
        return TIME_SCALE;
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

    private void prepareAgentsAndSetZone(double lat, double lon, int radius) {
        refreshTimer.cancel();
        refreshTimer = new Timer();
        configContainer.setZone(lat, lon, radius);

        if (masterAgent.prepareAgents()) {
            setState(SimulationState.READY_TO_RUN);
            refreshTimer.scheduleAtFixedRate(new RefreshTask(), 0, REFRESH_MAP_INTERVAL_MILLISECONDS);
        }
    }

    private int getCarLimit() {
        return (int) carLimitSpinner.getValue();
    }

    public int getTestCarId() {
        return (int) testCarIdSpinner.getValue();
    }

    private int getSeed() {
        return (int) seedSpinner.getValue();
    }

    public Date getSimulationStartTime() {
        return (Date) setTimeSpinner.getValue();
    }

    private void refreshTime() {
        Date date = getSimulationStartTime();
        Duration timeDiff = Duration.ofSeconds((TIME_SCALE * REFRESH_MAP_INTERVAL_MILLISECONDS) / 1000);
        Instant inst = date.toInstant();
        inst = inst.plus(timeDiff);
        DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss dd-MM-yyyy");
        String strDate = dateFormat.format(Date.from(inst));
        currentTimeLabel.setText(strDate);
        setTimeSpinner.setValue(Date.from(inst));
    }

    private void drawLights(List<Painter<JXMapViewer>> painters) {
        if (!configContainer.tryLockLightManagers()) {
            return;
        }

        agentsContainer.forEach(LightManager.class, man -> man.draw(painters));

        configContainer.unlockLightManagers();
    }

    private void drawVehicles(List<Painter<JXMapViewer>> painters) {
        try {
            Set<Waypoint> set = new HashSet<>();
            agentsContainer.forEach(VehicleAgent.class, a -> {
                var waypoint = new DefaultWaypoint(a.getVehicle().getPosition().toMapGeoPosition());
                if (a.getVehicle() instanceof TestCar) {
                    Set<Waypoint> testCarWaypoint = new HashSet<>();
                    testCarWaypoint.add(waypoint);

                    WaypointPainter<Waypoint> testPainter = new WaypointPainter<>();
                    testPainter.setWaypoints(testCarWaypoint);
                    testPainter.setRenderer(new CustomWaypointRenderer("test_car.png"));
                    painters.add(testPainter);
                }
                else {
                    set.add(waypoint);
                }
            });
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("cabriolet.png"));
            painters.add(waypointPainter);
        } catch (Exception e) {
            logger.warn("Error drawing vehicles", e);
        }
    }

    private void drawRoutes(List<Painter<JXMapViewer>> painters) {
        try {
            agentsContainer.forEach(VehicleAgent.class, a -> {
                List<IGeoPosition> track = new ArrayList<>(a.getVehicle().getDisplayRoute());

                Random r = new Random(a.hashCode());
                RoutePainter routePainter = new RoutePainter(track, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                painters.add(routePainter);
            });
        } catch (Exception e) {
            logger.warn("Error drawing routes", e);
        }
    }

    private void drawPedestrians(List<Painter<JXMapViewer>> painters) {
        try {
            Set<Waypoint> set = new HashSet<>();
            for (PedestrianAgent a : MasterAgent.pedestrians) {
                if (!a.isInBus()) {
                    var waypoint = new DefaultWaypoint(a.getPedestrian().getPosition().toMapGeoPosition());
                    if (a.getPedestrian() instanceof TestPedestrian) {
                        Set<Waypoint> testPedestrianWaypoint = new HashSet<>();
                        testPedestrianWaypoint.add(waypoint);

                        WaypointPainter<Waypoint> testPainter = new WaypointPainter<>();
                        testPainter.setWaypoints(testPedestrianWaypoint);
                        testPainter.setRenderer(new CustomWaypointRenderer("pedestrian_blue.png"));
                        painters.add(testPainter);
                    }
                    else {
                        set.add(waypoint);
                    }
                }
            }
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("pedestrian_small.png"));
            painters.add(waypointPainter);
        } catch (Exception e) {
            logger.warn("Error drawing pedestrians", e);
        }
    }

    private void drawPedestrianRoutes(List<Painter<JXMapViewer>> painters) {
        try {
            for (PedestrianAgent a : MasterAgent.pedestrians) {
                List<IGeoPosition> trackBefore = new ArrayList<>(a.getPedestrian().getDisplayRouteBeforeBus());
                List<IGeoPosition> trackAfter = new ArrayList<>(a.getPedestrian().getDisplayRouteAfterBus());

                Random r = new Random(a.hashCode());
                Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
                RoutePainter routePainterBefore = new RoutePainter(trackBefore, c);
                RoutePainter routePainterAfter = new RoutePainter(trackAfter, c);
                painters.add(routePainterBefore);
                painters.add(routePainterAfter);
            }
        } catch (Exception e) {
            logger.warn("Error drawing pedestrian routes", e);
        }
    }

    private void drawBuses(List<Painter<JXMapViewer>> painters) {
        try {
            Set<Waypoint> set_low = new HashSet<>();
            Set<Waypoint> set_mid = new HashSet<>();
            Set<Waypoint> set_high = new HashSet<>();
            agentsContainer.forEach(BusAgent.class, busAgent -> {
                var bus = busAgent.getBus();
                int count = bus.getPassengersCount();
                var pos = bus.getPosition().toMapGeoPosition();
                if (count > Bus.CAPACITY_HIGH) {
                    set_high.add(new DefaultWaypoint(pos));
                }
                else if (count > Bus.CAPACITY_MID) {
                    set_mid.add(new DefaultWaypoint(pos));
                }
                else {
                    set_low.add(new DefaultWaypoint(pos));
                }
            });

            WaypointPainter<Waypoint> painter_low = new WaypointPainter<>();
            painter_low.setWaypoints(set_low);
            painter_low.setRenderer(new CustomWaypointRenderer("bus_low.png"));
            painters.add(painter_low);

            WaypointPainter<Waypoint> painter_mid = new WaypointPainter<>();
            painter_mid.setWaypoints(set_mid);
            painter_mid.setRenderer(new CustomWaypointRenderer("bus.png"));
            painters.add(painter_mid);

            WaypointPainter<Waypoint> painter_high = new WaypointPainter<>();
            painter_high.setWaypoints(set_high);
            painter_high.setRenderer(new CustomWaypointRenderer("bus_high.png"));
            painters.add(painter_high);
        } catch (Exception e) {
            logger.warn("Error drawing buses", e);
        }
    }

    private void drawBusRoutes(List<Painter<JXMapViewer>> painters) {
        try {
            agentsContainer.forEach(BusAgent.class, busAgent -> {
                var bus = busAgent.getBus();
                List<IGeoPosition> track = new ArrayList<>(bus.getDisplayRoute());

                Random r = new Random(busAgent.hashCode());
                RoutePainter routePainter = new RoutePainter(track, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                painters.add(routePainter);

            });
        } catch (Exception e) {
            logger.error("Error drawing bus routes", e);
            renderBusRoutes = false;
        }
    }

    private void drawZones(Collection<Painter<JXMapViewer>> painters) {
        if (zone != null) {
            Set<Waypoint> set = new HashSet<>();
            set.add(new DefaultWaypoint(zone.getCenter().toMapGeoPosition()));
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("blue_waypoint.png"));
            painters.add(waypointPainter);

            painters.add(new ZonePainter(zone.getCenter(), getZoneRadius(), Color.BLUE));
        }
    }

    private void drawStations(List<Painter<JXMapViewer>> painters) {
        Set<Waypoint> set = new HashSet<>();
        for (OSMStation stationOSMNode : MasterAgent.osmIdToStationOSMNode.values()) {
            set.add(new DefaultWaypoint(stationOSMNode.toMapGeoPosition()));
        }
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(set);
        waypointPainter.setRenderer(new CustomWaypointRenderer("bus_stop.png"));
        painters.add(waypointPainter);
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

    private IGeoPosition generateRandomGeoPosOffsetWithRadius(final int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        if (getTestCarId() == masterAgent.carId) {
            angle = testCarRandom.nextDouble() * Math.PI * 2;
        }
        double lat = Math.sin(angle) * radius * 0.0000089;
        double lng = Math.cos(angle) * radius * 0.0000089 * Math.cos(lat);
        return Position.of(lat, lng);
    }

    public class RefreshTask extends TimerTask { // OCB ?????? TOO OFTEN
        @Override
        public void run() {
            try {
                List<Painter<JXMapViewer>> painters = new ArrayList<>();
                if (renderBusRoutes) {
                    drawBusRoutes(painters);
                }
                if (renderCarRoutes) {
                    drawRoutes(painters);
                }
                if (renderPedestrianRoutes) {
                    drawPedestrianRoutes(painters);
                }
                if (renderZone) {
                    drawZones(painters);
                }
                if (renderStations) {
                    drawStations(painters);
                }
                if (renderLights) {
                    drawLights(painters);
                }
                if (renderCars) {
                    drawVehicles(painters);
                }
                if (renderBuses) {
                    drawBuses(painters);
                }
                if (renderPedestrians) {
                    drawPedestrians(painters);
                }

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                MapViewer.setOverlayPainter(painter);
                if (state == SimulationState.RUNNING) {
                    refreshTime();
                }
            } catch (Exception e) {
                logger.error("Error refreshing simulation", e);
            }
        }
    }

    public class BusControlTask extends TimerTask { // OCB ?????? TOO OFTEN
        @Override
        public void run() {
            try {
                if (state == SimulationState.RUNNING) {
                    RunBusBasedOnTimeTable();
                }
            } catch (Exception e) {
                logger.warn("Error in bus control task", e);
            }
        }

        private void RunBusBasedOnTimeTable() {
            agentsContainer.forEach(BusAgent.class, busAgent -> busAgent.runBasedOnTimetable(getSimulationStartTime()));
        }
    }

    public class CreateCarTask extends TimerTask {

        @Override
        public void run() {
            if (!configContainer.shouldGenerateCars() || agentsContainer.size(VehicleAgent.class) >= getCarLimit()) {
                this.cancel();
            }

            var zoneCenter = zone.getCenter();
            var geoPosInZoneCircle = generateRandomGeoPosOffsetWithRadius(zone.getRadius());
            IGeoPosition posA = zoneCenter.sum(geoPosInZoneCircle);
            IGeoPosition posB = zoneCenter.diff(geoPosInZoneCircle);

            List<RouteNode> info;
            try {
                info = Router.generateRouteInfo(posA, posB);
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                this.cancel();
                return;
            }

            VehicleAgent vehicle;
            if (getTestCarId() == masterAgent.carId) {
                vehicle = masterAgent.tryAddNewVehicleAgent(info, true);
            }
            else {
                vehicle = masterAgent.tryAddNewVehicleAgent(info);
            }

            vehicle.start();
        }
    }

    public class CreatePedestrianTask extends TimerTask {

        @Override
        public void run() {
            if (!configContainer.shouldGeneratePedestriansAndBuses()) {
                this.cancel();
                return;
            }

            try {
                // TODO: add people limit
                final Pair<Pair<StationNode, StationNode>, String> stationNodePairAndBusLine = getStationPairAndLineFromRandomBus();
                final StationNode startStation = stationNodePairAndBusLine.getValue0().getValue0();
                final StationNode finishStation = stationNodePairAndBusLine.getValue0().getValue1();
                // TODO: Generating this offset doesn't work!
                var geoPosInFirstStationCircle = generateRandomGeoPosOffsetWithRadius(PEDESTRIAN_STATION_RADIUS);
                IGeoPosition pedestrianStartPoint = startStation.sum(geoPosInFirstStationCircle);
                IGeoPosition pedestrianFinishPoint = finishStation.sum(geoPosInFirstStationCircle);

                List<RouteNode> routeToStation = Router.generateRouteInfoForPedestrians(pedestrianStartPoint, startStation,
                        null, startStation.getOsmStationId());
                List<RouteNode> routeFromStation = Router.generateRouteInfoForPedestrians(finishStation, pedestrianFinishPoint,
                        finishStation.getOsmStationId(), null);


                if (getTestCarId() == masterAgent.pedestrianId) {
                    final TestPedestrian pedestrian = new TestPedestrian(routeToStation, routeFromStation, startStation.getStationId(), stationNodePairAndBusLine.getValue1(),
                            stationNodePairAndBusLine.getValue0().getValue0(), stationNodePairAndBusLine.getValue0().getValue1());
                    var agent = MasterAgent.tryAddNewPedestrianAgent(pedestrian);
                    agent.start();
                }
                else {
                    final Pedestrian pedestrian = new Pedestrian(routeToStation, routeFromStation, startStation.getStationId(), stationNodePairAndBusLine.getValue1(),
                            stationNodePairAndBusLine.getValue0().getValue0(), stationNodePairAndBusLine.getValue0().getValue1());
                    var agent = MasterAgent.tryAddNewPedestrianAgent(pedestrian);
                    agent.start();
                }
                masterAgent.pedestrianId++;
            } catch (Exception e) {
                logger.warn("Unknown error.", e);
                this.cancel();
            }
        }

        private Pair<Pair<StationNode, StationNode>, String> getStationPairAndLineFromRandomBus() {
            final BusAgent randomBusAgent = getRandomBusAgent();
            return Pair.with(randomBusAgent.getTwoSubsequentStations(random), randomBusAgent.getLine());
        }

        private BusAgent getRandomBusAgent() {
            final List<BusAgent> busArray = ImmutableList.copyOf(agentsContainer.iterator(BusAgent.class)); // TODO RETHINK!!!
            BusAgent bus;
            try {
                bus = busArray.get(random.nextInt(busArray.size()));
            } catch (Exception e) {
                logger.error("The 'shouldPrepareBuses' toggle in smartCityAgent is probably switched off (pedestrians " +
                        "cannot exist without buses)", e);
                throw e;
            }

            return bus;
        }
    }
}
