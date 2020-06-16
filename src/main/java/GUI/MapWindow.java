package GUI;

import Agents.BusAgent;
import Agents.LightManager;
import Agents.PedestrianAgent;
import Agents.StationAgent;
import Agents.VehicleAgent;
import OSMProxy.Elements.OSMStation;
import Routing.RouteNode;
import Routing.Router;
import Routing.StationNode;
import SmartCity.RoutePainter;
import SmartCity.SmartCityAgent;
import SmartCity.ZonePainter;
import Vehicles.MovingObjectImpl;
import Vehicles.Pedestrian;
import Vehicles.TestCar;
import jade.wrapper.StaleProxyException;

import org.javatuples.Pair;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.*;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class MapWindow {
    private final static int REFRESH_MAP_INTERVAL_MILLISECONDS = 100;
    private final static int BUS_CONTROL_INTERVAL_MILLISECONDS = 2000; //60000
    private final static int CREATE_CAR_INTERVAL_MILLISECONDS = 500;
	private static final long CREATE_PEDESTRIAN_INTERVAL_MILLISECONDS = 2000000000;
	private final static int PEDESTRIAN_STATION_RADIUS = 300;

    public JPanel MainPanel;
    public JXMapViewer MapViewer;
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
    private SmartCityAgent SmartCityAgent;
    private Timer refreshTimer = new Timer(true);
    private Timer spawnTimer = new Timer(true);
    private GeoPosition pointA;
    private GeoPosition pointB;
    private SimulationState state = SimulationState.SETTING_ZONE;
    private Random random = new Random();
    private GeoPosition zoneCenter;
    private Instant simulationStart;
    public boolean renderCars = true;
    public boolean renderCarRoutes = true;
    public boolean renderBuses = true;
    public boolean renderBusRoutes = true;
    public boolean renderZone = true;
    public boolean renderLights = true;
    public boolean renderStations = true;

    public MapWindow() {
        MapViewer = new JXMapViewer();
        currentTimeLabel.setVisible(false);
        currentTimeTitle.setVisible(false);
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        MapViewer.setTileFactory(tileFactory);
        GeoPosition warsaw = new GeoPosition(52.24, 21.02);
        MapViewer.setZoom(7);
        MapViewer.setAddressLocation(warsaw);
        radiusSpinner.setModel(new SpinnerNumberModel(100, 100, 50000, 100));
        carLimitSpinner.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
        carLimitSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (getCarLimit() - 1 < getTestCarId()) {
                    testCarIdSpinner.setValue(getCarLimit() - 1);
                }
            }
        });
        
       
        seedSpinner.setModel(new SpinnerNumberModel(69, 0, 999999, 1));
        latSpinner.setModel(new SpinnerNumberModel(52.205155, -90, 90, 1));
        lonSpinner.setModel(new SpinnerNumberModel(20.859244, -180, 180, 0.001));

        testCarIdSpinner.setModel(new SpinnerNumberModel(40, 0, 100, 1));
        testCarIdSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (getTestCarId() >= getCarLimit()) {
                    testCarIdSpinner.setValue(testCarIdSpinner.getPreviousValue());
                }
            }
        });
        setZoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareAgentsAndSetZone((double) latSpinner.getValue(), (double) lonSpinner.getValue(), getZoneRadius());
                state = SimulationState.READY_TO_RUN;
            }
        });

        setTimeSpinner.setModel(new SpinnerDateModel());
        JSpinner.DateEditor dateTimeEditor = new JSpinner.DateEditor(setTimeSpinner, "HH:mm:ss dd-MM-yyyy");
        setTimeSpinner.setEditor(dateTimeEditor);
        setTimeSpinner.setValue(new Date());

        //Left click event
        MapViewer.addMouseListener(new MapClickListener(MapViewer) {
            @Override
            public void mapClicked(GeoPosition geoPosition) {
                System.out.println("Lat: " + geoPosition.getLatitude() + " Lon: " + geoPosition.getLongitude());
                switch (state) {
                    case SETTING_ZONE:
                    case READY_TO_RUN:
                        latSpinner.setValue(geoPosition.getLatitude());
                        lonSpinner.setValue(geoPosition.getLongitude());
                        prepareAgentsAndSetZone(geoPosition.getLatitude(), geoPosition.getLongitude(), getZoneRadius());
                        state = SimulationState.READY_TO_RUN;
                        break;
                    case RUNNING:
                        if (pointA == null) {
                            pointA = geoPosition;
                        } else {
                            pointB = geoPosition;
                            //create car, generate lights, add route to car, add car to agents
                            VehicleAgent vehicle = new VehicleAgent();
                            List<RouteNode> info = Router.generateRouteInfo(pointA, pointB);
                            MovingObjectImpl car = new MovingObjectImpl(info);
                            vehicle.setVehicle(car);
                            try {
                                SmartCityAgent.AddNewVehicleAgent(car.getVehicleType() + SmartCityAgent.Vehicles.size(), vehicle);
                                SmartCityAgent.ActivateAgent(vehicle);
                            } catch (StaleProxyException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Vehicles: " + SmartCityAgent.Vehicles.size());
                            System.out.println("Lights: " + SmartCityAgent.lightManagers.size());
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
        StartRouteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (state != SimulationState.READY_TO_RUN) return;
                setInputEnabled(false);
                currentTimeTitle.setVisible(true);
                currentTimeLabel.setVisible(true);
                SmartCityAgent.activateLightManagerAgents();
               
                spawnTimer.scheduleAtFixedRate(new CreateCarTask(), 0, CREATE_CAR_INTERVAL_MILLISECONDS);
                if (SmartCity.SmartCityAgent.shouldGeneratePedestrians)
                	spawnTimer.scheduleAtFixedRate(new CreatePedestrianTask(), 0, CREATE_PEDESTRIAN_INTERVAL_MILLISECONDS);
                simulationStart = Instant.now();
                refreshTimer.scheduleAtFixedRate(new BusControlTask(), 0,BUS_CONTROL_INTERVAL_MILLISECONDS);
                state = SimulationState.RUNNING;
            }
        });
        refreshTimer.scheduleAtFixedRate(new RefreshTask(), 0, REFRESH_MAP_INTERVAL_MILLISECONDS);
       
    }

    public void setInputEnabled(boolean check) {
        carLimitSpinner.setEnabled(check);
        seedSpinner.setEnabled(check);
        radiusSpinner.setEnabled(check);
        latSpinner.setEnabled(check);
        lonSpinner.setEnabled(check);
        testCarIdSpinner.setEnabled(check);
        setZoneButton.setEnabled(check);
        StartRouteButton.setEnabled(check);
        setTimeSpinner.setEnabled(check);
    }

    public void prepareAgentsAndSetZone(double lat, double lon, int radius) {
        refreshTimer.cancel();
        refreshTimer = new Timer();

        zoneCenter = new GeoPosition(lat, lon);

        if (SmartCityAgent.shouldPrepareBuses)
            SmartCityAgent.prepareStationsAndBuses(zoneCenter, getZoneRadius());
        SmartCityAgent.prepareLightManagers(zoneCenter, getZoneRadius());
        state = SimulationState.READY_TO_RUN;

        refreshTimer.scheduleAtFixedRate(new RefreshTask(), 0, REFRESH_MAP_INTERVAL_MILLISECONDS);
    }

    public MapWindow(SmartCityAgent agent) {
        this();
        SmartCityAgent = agent;
    }

    private int getZoneRadius() {
        return (int) radiusSpinner.getValue();
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

    private Date getSimulationStartTime() {
        return (Date) setTimeSpinner.getValue();
    }

    private void RefreshTime() {
        Date date = getSimulationStartTime();
        //Duration timeDiff = Duration.between(simulationStart, Instant.now());
        Duration timeDiff = Duration.ofSeconds(3);
        Instant inst = date.toInstant();
        inst = inst.plus(timeDiff);
        DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss dd-MM-yyyy");
        String strDate = dateFormat.format(Date.from(inst));
        currentTimeLabel.setText(strDate);
        setTimeSpinner.setValue(Date.from(inst));

    }

    public void DrawLights(List<Painter<JXMapViewer>> painters) {
        if (SmartCity.SmartCityAgent.lightManagersUnderConstruction)
            return;

        for (LightManager mgr : SmartCity.SmartCityAgent.lightManagers) {
            mgr.draw(painters);
        }
    }

    public void DrawVehicles(List painters) {
        try {
            Set<Waypoint> set = new HashSet<>();
            for (VehicleAgent a : SmartCityAgent.Vehicles) {
                if (a.getVehicle() instanceof TestCar) {
                    Set<Waypoint> testCarWaypoint = new HashSet<>();
                    testCarWaypoint.add(new DefaultWaypoint(a.getVehicle().getPosition()));

                    WaypointPainter<Waypoint> testPainter = new WaypointPainter<>();
                    testPainter.setWaypoints(testCarWaypoint);
                    testPainter.setRenderer(new CustomWaypointRenderer("test_car.png"));
                    painters.add(testPainter);
                } else set.add(new DefaultWaypoint(a.getVehicle().getPosition()));
            }
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("cabriolet.png"));
            painters.add(waypointPainter);
        } catch (Exception e) {
        	
        }
    }
    
    public void DrawPedestrians(List painters) {
        try {
            Set<Waypoint> set = new HashSet<>();
            for (PedestrianAgent a : SmartCityAgent.pedestrians) {
                set.add(new DefaultWaypoint(a.getPedestrian().getPosition()));
            }
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("pedestrian.png"));
            painters.add(waypointPainter);
        } catch (Exception e) {

        }
    }

    public void DrawRoutes(List painters) {
        try {
            for (VehicleAgent a : SmartCityAgent.Vehicles) {
                List<GeoPosition> track = new ArrayList<GeoPosition>();
                for (RouteNode point : a.getVehicle().getDisplayRoute()) {
                    track.add(new GeoPosition(point.getLatitude(), point.getLongitude()));
                }
                Random r = new Random(a.hashCode());
                RoutePainter routePainter = new RoutePainter(track, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                painters.add(routePainter);
            }
        } catch (Exception e) {

        }
    }

    public void DrawBuses(List painters) {
        try {
            Set<Waypoint> set = new HashSet<>();
            for (BusAgent a : SmartCityAgent.buses) {
                set.add(new DefaultWaypoint(a.getBus().getPosition()));
            }
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("bus.png"));
            painters.add(waypointPainter);
        } catch (Exception e) {

        }
    }

    public void DrawBusRoutes(List painters) {
        try {
            for (BusAgent a : SmartCityAgent.buses) {
                List<GeoPosition> track = new ArrayList<GeoPosition>();
                for (RouteNode point : a.getBus().getDisplayRoute()) {
                    track.add(new GeoPosition(point.getLatitude(), point.getLongitude()));
                }
                Random r = new Random(a.hashCode());
                RoutePainter routePainter = new RoutePainter(track, new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                painters.add(routePainter);

            }

        } catch (Exception e) {

        }
    }

    public void DrawZones(List painters) {
        if (zoneCenter != null) {
            Set<Waypoint> set = new HashSet<>();
            set.add(new DefaultWaypoint(zoneCenter));
            WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(set);
            waypointPainter.setRenderer(new CustomWaypointRenderer("blue_waypoint.png"));
            painters.add(waypointPainter);

            painters.add(new ZonePainter(zoneCenter, getZoneRadius(), Color.BLUE));
        }
    }

    private void DrawStations(List painters) {
        Set<Waypoint> set = new HashSet<>();
        for (OSMStation stationOSMNode : SmartCityAgent.osmIdToStationOSMNode.values()) {
            set.add(new DefaultWaypoint(stationOSMNode.getPosition()));
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
        SidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer1, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Car limit");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label1, gbc);
        carLimitSpinner = new JSpinner();
        carLimitSpinner.setMaximumSize(new Dimension(150, 30));
        carLimitSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(carLimitSpinner, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Simulation seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label2, gbc);
        seedSpinner = new JSpinner();
        seedSpinner.setMaximumSize(new Dimension(150, 30));
        seedSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(seedSpinner, gbc);
        StartRouteButton = new JButton();
        StartRouteButton.setText("Start vehicles");
        StartRouteButton.putClientProperty("hideActionText", Boolean.FALSE);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
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
        gbc.gridy = 11;
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
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(label7, gbc);
        testCarIdSpinner = new JSpinner();
        testCarIdSpinner.setMaximumSize(new Dimension(150, 30));
        testCarIdSpinner.setMinimumSize(new Dimension(150, 30));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        SidePanel.add(testCarIdSpinner, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Simulation time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(label8, gbc);
        setTimeSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SidePanel.add(setTimeSpinner, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(25, 0, 0, 0);
        SidePanel.add(spacer3, gbc);
        currentTimeLabel = new JLabel();
        currentTimeLabel.setHorizontalAlignment(0);
        currentTimeLabel.setHorizontalTextPosition(0);
        currentTimeLabel.setText("...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        SidePanel.add(currentTimeLabel, gbc);
        currentTimeTitle = new JLabel();
        currentTimeTitle.setText("Current time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.WEST;
        SidePanel.add(currentTimeTitle, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return MainPanel;
    }

    public class RefreshTask extends TimerTask { // OCB ?????? TOO OFTEN
        @Override
        public void run() {
            try {

                List<Painter<JXMapViewer>> painters = new ArrayList<>();
                if (renderBusRoutes) DrawBusRoutes(painters);
                if (renderCarRoutes) DrawRoutes(painters);
                if (renderZone) DrawZones(painters);
                if (renderStations) DrawStations(painters);
                if (renderLights) DrawLights(painters);
                if (renderCars) DrawVehicles(painters);
                if (renderBuses) DrawBuses(painters);
                
                DrawPedestrians(painters);
                
                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                MapViewer.setOverlayPainter(painter);
                if (state == SimulationState.RUNNING) RefreshTime();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }

    public class BusControlTask extends TimerTask { // OCB ?????? TOO OFTEN
        @Override
        public void run() {
            try {             
                if (state == SimulationState.RUNNING) RunBusBasedOnTimeTable();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }

		private void RunBusBasedOnTimeTable() {
			
			for (BusAgent busAgent : SmartCityAgent.buses) {
				busAgent.runBasedOnTimetable(getSimulationStartTime());
			}
			
		}
    }
    
    
    public class CreateCarTask extends TimerTask {

        @Override
        public void run() {
            if (SmartCityAgent.Vehicles.size() >= getCarLimit())
            	return;
            final Pair<Double, Double> geoPosInZoneCircle = generateRandomGeoPosOffsetWithRadius(getZoneRadius());
            GeoPosition A = new GeoPosition(zoneCenter.getLatitude() + geoPosInZoneCircle.getValue0(),
                    zoneCenter.getLongitude() + geoPosInZoneCircle.getValue1());
            GeoPosition B = new GeoPosition(zoneCenter.getLatitude() - geoPosInZoneCircle.getValue0(),
                    zoneCenter.getLongitude() - geoPosInZoneCircle.getValue1());
            List<RouteNode> info;
            try {
                info = Router.generateRouteInfo(A, B);//(new GeoPosition(52.179977, 21.071040), new GeoPosition(52.178759, 21.070854));//(new GeoPosition(52.228275, 20.986557), new GeoPosition(52.234908, 20.981210));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            VehicleAgent vehicle = new VehicleAgent();
            MovingObjectImpl car;
            if (getTestCarId() == SmartCityAgent.Vehicles.size())
                car = new TestCar(info);
            else
            	car = new MovingObjectImpl(info);
            vehicle.setVehicle(car);
            try {
                SmartCityAgent.AddNewVehicleAgent(car.getVehicleType() + SmartCityAgent.carId, vehicle);
                SmartCityAgent.carId++;
                SmartCityAgent.ActivateAgent(vehicle);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            SmartCityAgent.ActivateAgent(vehicle);
        }
    }

    public class CreatePedestrianTask extends TimerTask {

        @Override
        public void run() {
            // add people limit
            final Pair<Pair<StationNode, StationNode>, String> stationNodePairAndBusLine = getStationPairAndLineFromRandomBus();
            final StationNode startStation = stationNodePairAndBusLine.getValue0().getValue0();
            final StationNode finishStation = stationNodePairAndBusLine.getValue0().getValue1();
            final Pair<Double, Double> geoPosInFirstStationCircle = generateRandomGeoPosOffsetWithRadius(MapWindow.PEDESTRIAN_STATION_RADIUS);
            GeoPosition pedestrianStartPoint = new GeoPosition(startStation.getLatitude() + geoPosInFirstStationCircle.getValue0(),
                    startStation.getLongitude() + geoPosInFirstStationCircle.getValue1());
            GeoPosition pedestrianGetOnStation = new GeoPosition(startStation.getLatitude(), finishStation.getLongitude());
            GeoPosition pedestrianDisembarkStation = new GeoPosition(startStation.getLatitude(), finishStation.getLongitude());
            GeoPosition pedestrianFinishPoint = new GeoPosition(finishStation.getLatitude() + geoPosInFirstStationCircle.getValue0(),
                    finishStation.getLongitude() + geoPosInFirstStationCircle.getValue1());
            List<RouteNode> routeToStation = Router.generateRouteInfoForPedestrians(pedestrianStartPoint, pedestrianGetOnStation);
            List<RouteNode> routeFromStation = Router.generateRouteInfoForPedestrians(pedestrianDisembarkStation, pedestrianFinishPoint);
            final Pedestrian pedestrian = new Pedestrian(routeFromStation, routeToStation, startStation.getStationId(), stationNodePairAndBusLine.getValue1());
            SmartCityAgent.ActivateAgent(SmartCity.SmartCityAgent.tryAddNewPedestrianAgent(pedestrian));
        }

        private Pair<Pair<StationNode, StationNode>, String> getStationPairAndLineFromRandomBus() {
            final BusAgent randomBusAgent = getRandomBusAgent();
            return Pair.with(randomBusAgent.getTwoSubsequentStations(random), randomBusAgent.getLine());
        }

        private BusAgent getRandomBusAgent() {
            final List<BusAgent> busArray = new ArrayList<>(SmartCity.SmartCityAgent.buses); // TODO RETHINK!!!
            try {
            	return busArray.get(random.nextInt(busArray.size()));
            } catch (Exception e) {
            	try {
            		throw new Exception("The 'shouldPrepareBuses' toggle in SmartCityAgent is probably switched off (pedestrians cannot exist without buses)");
            	} catch (Exception e2) {
            		e2.printStackTrace();
            		return null;
            	}
            }
        }
    }

    private Pair<Double, Double> generateRandomGeoPosOffsetWithRadius(final int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double lat = Math.sin(angle) * radius * 0.0000089;
        double lon = Math.cos(angle) * radius * 0.0000089 * Math.cos(lat);
        return Pair.with(lat, lon);
    }
}
