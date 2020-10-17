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
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.BitUtil;
import gnu.trove.set.TIntSet;

import java.util.Collection;
import java.util.List;

/**
 * @author Peter Karich
 */
public class ExtendedGraphHopper extends GraphHopper {

    private static final double HEAVY_EDGE_PENALTY_FACTOR = 999999999;

    // mapping of internal edge ID to OSM way ID
    private DataAccess edgeMapping;
    private BitUtil bitUtil;
    private static AvoidEdgesRemovableWeighting avoidEdgesWeighting = null;

    @Override
    public boolean load(String graphHopperFolder) {
        boolean loaded = super.load(graphHopperFolder);

        Directory dir = getGraphHopperStorage().getDirectory();
        bitUtil = BitUtil.get(dir.getByteOrder());
        edgeMapping = dir.find("edge_mapping");

        if (loaded) {
            edgeMapping.loadExisting();
        }

        return loaded;
    }

    /* TODO: Utilize edge functions in the trouble generating strategy */

    public static final void addForbiddenEdges(final Collection<Integer> edgeIds) {
        avoidEdgesWeighting.addEdgeIds(edgeIds);
    }

    public static final void removeForbiddenEdges(final Collection<Integer> edgeIds) {
        avoidEdgesWeighting.removeEdgeIds(edgeIds);
    }

    public static final TIntSet getForbiddenEdges() {
        return avoidEdgesWeighting.getEdgeIds();
    }

    @Override
    public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder) {
        String weightingStr = hintsMap.getWeighting().toLowerCase();
        if (AvoidEdgesRemovableWeighting.NAME.equals(weightingStr)) {
            if (avoidEdgesWeighting == null) {
                avoidEdgesWeighting = new AvoidEdgesRemovableWeighting(new FastestWeighting(encoder));
                avoidEdgesWeighting.setEdgePenaltyFactor(HEAVY_EDGE_PENALTY_FACTOR);
            }
            return avoidEdgesWeighting;
        }
        else {
            return super.createWeighting(hintsMap, encoder);
        }
    }

    @Override
    protected DataReader createReader(GraphHopperStorage ghStorage) {
        OSMReader reader = new OSMReader(ghStorage) {
            {
                edgeMapping.create(1000);
            }

            // this method is only in >0.6 protected, before it was private
            @Override
            protected void storeOsmWayID(int edgeId, long osmWayId) {
                super.storeOsmWayID(edgeId, osmWayId);

                long pointer = 8L * edgeId;
                edgeMapping.ensureCapacity(pointer + 8L);

                edgeMapping.setInt(pointer, bitUtil.getIntLow(osmWayId));
                edgeMapping.setInt(pointer + 4, bitUtil.getIntHigh(osmWayId));
            }

            @Override
            protected void finishedReading() {
                super.finishedReading();

                edgeMapping.flush();
            }
        };

        return initDataReader(reader);
    }

    long getOSMWay(int internalEdgeId) {
        long pointer = 8L * internalEdgeId;
        return bitUtil.combineIntsToLong(edgeMapping.getInt(pointer), edgeMapping.getInt(pointer + 4L));
    }

    @Override
    public List<Path> calcPaths(GHRequest request, GHResponse rsp) {
        return super.calcPaths(request, rsp);
    }
}
