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
package osmproxy.routes;

import com.google.inject.Inject;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import org.javatuples.Pair;
import osmproxy.routes.abstractions.IGraphHopper;
import osmproxy.routes.abstractions.IHighwayAccessor;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Start with the following command line settings:
 * <p>
 * config=config.properties osmreader.osm=area.pbf
 *
 * @author Peter Karich
 */
class HighwayAccessor implements IHighwayAccessor {
    private final IGraphHopper graphHopper;
    private final IRouteTransformer routeTransformer;

    @Inject
    HighwayAccessor(IGraphHopper graphHopper,
                    IRouteTransformer routeTransformer) {
        this.graphHopper = graphHopper;
        this.routeTransformer = routeTransformer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<List<Long>, List<Integer>> getOsmWayIdsAndEdgeList(IGeoPosition from,
                                                                   IGeoPosition to,
                                                                   String typeOfVehicle,
                                                                   boolean bewareOfJammedRoutes) {
        GHResponse response = new GHResponse();
        GHRequest request = new GHRequest(from.getLat(), from.getLng(), to.getLat(), to.getLng()).setVehicle(typeOfVehicle);
        String weighting = bewareOfJammedRoutes ? AvoidEdgesRemovableWeighting.NAME : "fastest";
        request = request.setWeighting(weighting);
        return calculatePaths(request, response);
    }

    private Pair<List<Long>, List<Integer>> calculatePaths(GHRequest req, GHResponse resp) {
        List<Long> osmWayIds = new ArrayList<>();
        List<Integer> edgeList = new ArrayList<>();
        List<Path> paths = graphHopper.calcPaths(req, resp);
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
            edgeList.addAll(getEdgeList(edgeId, edge.fetchWayGeometry(3)));
        }

        return new Pair<>(osmWayIds, edgeList);
    }

    private List<Integer> getEdgeList(int edgeId, PointList pointList) {
        List<Integer> edgeList = new ArrayList<>();
        List<RouteNode> temporaryList = pointListToNodeList(pointList);
        temporaryList = routeTransformer.uniformRoute(temporaryList);
        for (int i = 0; i < temporaryList.size(); ++i) {
            edgeList.add(edgeId);
        }
        return edgeList;
    }

    private static List<RouteNode> pointListToNodeList(PointList pointList) {
        List<RouteNode> list = new ArrayList<>();
        for (int i = 0; i < pointList.size(); ++i) {
            list.add(new RouteNode(pointList.toGHPoint(i).getLat(), pointList.toGHPoint(i).getLon()));
        }
        return list;
    }
}
