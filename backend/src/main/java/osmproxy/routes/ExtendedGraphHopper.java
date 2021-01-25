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
import osmproxy.routes.abstractions.IGraphHopper;

import java.util.Collection;
import java.util.List;

class ExtendedGraphHopper extends GraphHopper implements IGraphHopper {
    private static final double HEAVY_EDGE_PENALTY_FACTOR = 99999999;
    private final long pointerFactor = 8L;

    // mapping of internal edge ID to OSM way ID
    private DataAccess edgeMapping;
    private BitUtil bitUtil;
    private AvoidEdgesRemovableWeighting avoidEdgesWeighting;

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

    // TODO: Utilize edge functions in the trouble generating strategy
    @Override
    public void addForbiddenEdges(final Collection<Integer> edgeIds) {
        avoidEdgesWeighting.addEdgeIds(edgeIds);
    }

    @Override
    public void removeForbiddenEdges(final Collection<Integer> edgeIds) {
        avoidEdgesWeighting.removeEdgeIds(edgeIds);
    }

    @Override
    public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder) {
        String weightingStr = hintsMap.getWeighting().toLowerCase();
        if (avoidEdgesWeighting == null) {
            avoidEdgesWeighting = new AvoidEdgesRemovableWeighting(new FastestWeighting(encoder));
            avoidEdgesWeighting.setEdgePenaltyFactor(HEAVY_EDGE_PENALTY_FACTOR);
        }
        if (AvoidEdgesRemovableWeighting.NAME.equals(weightingStr)) {
            return avoidEdgesWeighting;
        }
        else {
            return super.createWeighting(hintsMap, encoder);
        }
    }

    @Override
    protected DataReader createReader(GraphHopperStorage ghStorage) {
        var reader = new OSMReader(ghStorage) {
            {
                edgeMapping.create(1000);
            }

            // this method is only in >0.6 protected, before it was private
            @Override
            protected void storeOsmWayID(int edgeId, long osmWayId) {
                super.storeOsmWayID(edgeId, osmWayId);

                long pointer = pointerFactor * edgeId;
                edgeMapping.ensureCapacity(pointer + pointerFactor);

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

    @Override
    public List<Path> calcPaths(GHRequest request, GHResponse ghRsp) {
        return super.calcPaths(request, ghRsp);
    }

    @Override
    public long getOSMWay(int internalEdgeId) {
        long pointer = pointerFactor * internalEdgeId;
        return bitUtil.combineIntsToLong(edgeMapping.getInt(pointer), edgeMapping.getInt(pointer + 4L));
    }
}
