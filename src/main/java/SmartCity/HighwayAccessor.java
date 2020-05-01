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
package SmartCity;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start with the following command line settings:
 *
 * config=config.properties osmreader.osm=area.pbf
 *
 * @author Peter Karich
 */
public class HighwayAccessor {

    public static Pair<List<Long>, PointList> getOsmWayIdsAndPointList(String[] args, double fromLat, double fromLon, double toLat, double toLon) {
        List<Long> osmWayIds = new ArrayList<>();
        PointList pointList = new PointList();
    	
    	MyGraphHopper graphHopper = new MyGraphHopper();
        graphHopper.init(CmdArgs.read(args));
        graphHopper.importOrLoad();

        GHResponse rsp = new GHResponse();
        List<Path> paths = graphHopper.calcPaths(new GHRequest(fromLat, fromLon, toLat, toLon).
                setWeighting("fastest").setVehicle("car"), rsp);
        Path path0 = paths.get(0);
        
        long previousWayId = 0;
        for (EdgeIteratorState edge : path0.calcEdges()) {
            int edgeId = edge.getEdge();
            String vInfo = "";
            if (edge instanceof VirtualEdgeIteratorState) {
                // first, via and last edges can be virtual
                VirtualEdgeIteratorState vEdge = (VirtualEdgeIteratorState) edge;
                edgeId = vEdge.getOriginalTraversalKey() / 2;
                vInfo = "v";
            }

            long osmWayIdToAdd = graphHopper.getOSMWay(edgeId);
            // deleting duplicates
            if (osmWayIdToAdd != previousWayId) {
                osmWayIds.add(osmWayIdToAdd);
                previousWayId = osmWayIdToAdd;
            }
            
            pointList.add(edge.fetchWayGeometry(2));
        }
        
        return new Pair<List<Long>, PointList>(osmWayIds, pointList);
    }
}
