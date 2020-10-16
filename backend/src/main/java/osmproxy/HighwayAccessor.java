/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package osmproxy;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import gnu.trove.set.TIntSet;

import org.javatuples.Pair;
import routing.nodes.RouteNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Start with the following command line settings:
 * <p>
 * config=config.properties osmreader.osm=area.pbf
 *
 * @author Peter Karich
 */
public class HighwayAccessor {
    private static final String CONFIG_PATH = "config/graphHopper.properties";
    private static final String[] args = new String[]{"config=" + CONFIG_PATH, "datareader.file=mazowieckie-latest.osm.pbf"};
    private static final ExtendedGraphHopper graphHopper;

    static {
        graphHopper = new ExtendedGraphHopper();
        graphHopper.init(CmdArgs.read(args));
        graphHopper.importOrLoad();
    }
    
    // TODO: tested manually, but add unit tests / integration tests

    public static Pair<List<Long>, List<RouteNode>> getOsmWayIdsAndPointList(double fromLat, double fromLon,
            double toLat, double toLon, boolean onFoot) {
        List<Long> osmWayIds = new ArrayList<>();
        List<RouteNode> pointList = new ArrayList<>();

        GHResponse response = new GHResponse();
        GHRequest request = new GHRequest(fromLat, fromLon, toLat, toLon)
        		.setVehicle(onFoot ? "foot" : "car")
        		.setWeighting(AvoidEdgesRemovableWeighting.NAME);
        List<Path> paths = graphHopper.calcPaths(request, response);
        Path path0 = paths.get(0);

        long previousWayId = 0;
        for (EdgeIteratorState edge : path0.calcEdges()) {
            int edgeId = edge.getEdge();
            if (edge instanceof VirtualEdgeIteratorState) {
                // first, via and last edges can be virtual
                VirtualEdgeIteratorState vEdge = (VirtualEdgeIteratorState) edge;
                edgeId = vEdge.getOriginalTraversalKey() / 2;
            }

            long osmWayIdToAdd = graphHopper.getOSMWay(edgeId);
            // deleting duplicates
            if (osmWayIdToAdd != previousWayId) {
                osmWayIds.add(osmWayIdToAdd);
                previousWayId = osmWayIdToAdd;
            }

            pointList.addAll(getRouteNodeList(edgeId, edge.fetchWayGeometry(2)));
        }
        
        return new Pair<>(osmWayIds, pointList);
    }

    private static List<RouteNode> getRouteNodeList(int edgeId, PointList pointList) {
        List<RouteNode> nodeList = new ArrayList<>();
        for (int i = 0; i < pointList.size(); ++i) {
            nodeList.add(new RouteNode(pointList.toGHPoint(i).lat, pointList.toGHPoint(i).lon, edgeId));
        }
        return nodeList;
    }

}
