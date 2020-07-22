package smartcity;

import agents.VehicleAgent;
import gui.MapWindow;
import org.jxmapviewer.viewer.GeoPosition;
import routing.RouteNode;
import routing.Router;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import static smartcity.MainContainerAgent.SHOULD_GENERATE_CARS;
import static smartcity.MainContainerAgent.SHOULD_GENERATE_PEDESTRIANS_AND_BUSES;

public class WindowInitializer {

    public static MapWindow displayWindow(MainContainerAgent smartCityAgent) {
        var window = new MapWindow(smartCityAgent);
        var mapViewer = window.MapViewer;
        JFrame frame = new JFrame("Smart City by Katherine & Dominic & Robert");
        frame.getContentPane().add(window.MainPanel);
        JMenuBar menuBar = new JMenuBar();
        JMenu view = new JMenu("View");

        final JCheckBoxMenuItem cars = new JCheckBoxMenuItem("Render cars", window.renderCars);
        cars.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderCars = cars.getState();
            }
        });
        view.add(cars);

        final JCheckBoxMenuItem routes = new JCheckBoxMenuItem("Render car routes", window.renderCarRoutes);
        routes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderCarRoutes = routes.getState();
            }
        });
        view.add(routes);

        final JCheckBoxMenuItem buses = new JCheckBoxMenuItem("Render buses", window.renderBuses);
        buses.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderBuses = buses.getState();
            }
        });
        view.add(buses);

        final JCheckBoxMenuItem busRoutes = new JCheckBoxMenuItem("Render bus routes", window.renderBusRoutes);
        busRoutes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderBusRoutes = busRoutes.getState();
            }
        });
        view.add(busRoutes);

        final JCheckBoxMenuItem pedestrian = new JCheckBoxMenuItem("Render pedestrians", window.renderPedestrians);
        pedestrian.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderPedestrians = pedestrian.getState();
            }
        });
        view.add(pedestrian);

        final JCheckBoxMenuItem pedestrianRoutes = new JCheckBoxMenuItem("Render pedestrian routes", window.renderPedestrianRoutes);
        pedestrianRoutes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderPedestrianRoutes = pedestrianRoutes.getState();
            }
        });
        view.add(pedestrianRoutes);

        final JCheckBoxMenuItem lights = new JCheckBoxMenuItem("Render lights", window.renderLights);
        lights.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderLights = lights.getState();
            }
        });
        view.add(lights);

        final JCheckBoxMenuItem zone = new JCheckBoxMenuItem("Render zone", window.renderZone);
        zone.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderZone = zone.getState();
            }
        });
        view.add(zone);

        final JCheckBoxMenuItem stations = new JCheckBoxMenuItem("Render stations", window.renderStations);
        stations.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderStations = stations.getState();
            }
        });
        view.add(stations);

        menuBar.add(view);

        JMenu debug = new JMenu("Debug");

        JMenuItem runTest = new JMenuItem("Test crossroad");
        runTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setInputEnabled(false);
                double lat = 52.23702507833161;
                double lon = 21.017934679985046;
                mapViewer.setAddressLocation(new GeoPosition(lat, lon));
                mapViewer.setZoom(1);
                window.prepareAgentsAndSetZone(lat, lon, 100);
                GeoPosition N = new GeoPosition(52.23758683540269, 21.017720103263855);
                GeoPosition S = new GeoPosition(52.23627934304847, 21.018092930316925);
                GeoPosition E = new GeoPosition(52.237225472020704, 21.019399166107178);
                GeoPosition W = new GeoPosition(52.23678526174392, 21.016663312911987);

                // N to S
                List<RouteNode> NS;
                try {
                    NS = Router.generateRouteInfo(N, S);

                    for (int i = 0; i < 5; i++) {
                        smartCityAgent.addNewCarAgent(NS);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // S to N
                List<RouteNode> SN;
                try {
                    SN = Router.generateRouteInfo(S, N);

                    for (int i = 0; i < 5; i++) {
                        smartCityAgent.addNewCarAgent(SN);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // E to W
                List<RouteNode> EW;
                try {
                    EW = Router.generateRouteInfo(E, W);

                    for (int i = 0; i < 5; i++) {
                        smartCityAgent.addNewCarAgent(EW);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // W to E
                List<RouteNode> WE;
                try {
                    WE = Router.generateRouteInfo(W, E);
                    for (int i = 0; i < 5; i++) {
                        smartCityAgent.addNewCarAgent(WE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                smartCityAgent.activateLightManagerAgents();

                // start all
                for (VehicleAgent agent : smartCityAgent.Vehicles) {
                    agent.start();
                }

            }
        });
        debug.add(runTest);

        menuBar.add(debug);

        WindowInitializer.addGenerationMenu(menuBar);

        frame.setJMenuBar(menuBar);
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        return window;
    }

    private static void addGenerationMenu(JMenuBar menuBar) {
        JMenu generation = new JMenu("Generation");

        final JCheckBoxMenuItem car_gen = new JCheckBoxMenuItem("Cars", SHOULD_GENERATE_CARS);
        car_gen.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SHOULD_GENERATE_CARS = car_gen.getState();
            }
        });
        generation.add(car_gen);

        final JCheckBoxMenuItem pedestrians = new JCheckBoxMenuItem("Pedestrians", SHOULD_GENERATE_PEDESTRIANS_AND_BUSES);
        pedestrians.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SHOULD_GENERATE_PEDESTRIANS_AND_BUSES = pedestrians.getState();
            }
        });
        generation.add(pedestrians);

        menuBar.add(generation);
    }
}
