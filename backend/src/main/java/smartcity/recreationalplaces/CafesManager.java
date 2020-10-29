package smartcity.recreationalplaces;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.BusLinesManager;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IBusLinesManager;
import osmproxy.buses.data.BusPreparationData;
import routing.core.IZone;

import java.util.HashSet;
import java.util.Set;

public class CafesManager  implements ICafesManager {

        private static final Logger logger = LoggerFactory.getLogger(CafesManager.class);

        private final ICafesApiManager cafeApiManager;
        private final IZone zone;

        @Inject
        CafesManager(ICafesApiManager cafeApiManager,
                        IZone zone) {
            this.cafeApiManager = cafeApiManager;
            this.zone = zone;
        }

        @Override
        public Set<OSMCafe> getCafesData() {
            var overpassInfo = cafeApiManager.getCafesDataXml(zone);
            if (overpassInfo.isEmpty()) {
                return new HashSet<OSMCafe>();
            }

            return cafeApiManager.parseCafeInfo(overpassInfo.get());
        }
    }


