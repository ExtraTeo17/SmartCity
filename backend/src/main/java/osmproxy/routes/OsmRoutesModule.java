package osmproxy.routes;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.graphhopper.util.CmdArgs;
import genesis.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.routes.abstractions.IGraphHopper;
import osmproxy.routes.abstractions.IHighwayAccessor;

import java.nio.file.Files;
import java.nio.file.Paths;

public class OsmRoutesModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(OsmRoutesModule.class);
    private static final String GRAPH_HOPPER_PROPERTIES_FILE = "graphHopper.properties";
    private static final String OSM_DATA_FILE = "mazowieckie-latest.osm.pbf";

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IGraphHopper.class).to(ExtendedGraphHopper.class).in(Singleton.class);
        binder.bind(IHighwayAccessor.class).to(HighwayAccessor.class).in(Singleton.class);
    }

    @Provides
    public ExtendedGraphHopper createGraphHopper() {
        var cmdArgs = readArgs();
        try {
            var graphHopper = new ExtendedGraphHopper();
            graphHopper.init(cmdArgs);
            graphHopper.importOrLoad();
            return graphHopper;
        } catch (Exception e) {
            logger.error("Error in graphHopper initialization", e);
            throw e;
        }
    }

    private static CmdArgs readArgs() {
        try {
            var configResource = OsmRoutesModule.class.getClassLoader().getResource("");
            if (configResource == null) {
                logger.error("Didn't find the loader resource");
                return new CmdArgs();
            }

            var mainPath = Paths.get(configResource.toURI()).toString();
            var configPath = Paths.get(mainPath, GRAPH_HOPPER_PROPERTIES_FILE);
            if (!Files.exists(configPath)) {
                logger.error("Didn't find the " + GRAPH_HOPPER_PROPERTIES_FILE + " file");
                return new CmdArgs();
            }

            var osmDataPath = Paths.get(mainPath, OSM_DATA_FILE);
            var args = new String[]{"config=" + configPath, "datareader.file=" + osmDataPath};
            return CmdArgs.read(args);
        } catch (Exception e) {
            logger.error("Error in reading args", e);
        }

        return new CmdArgs();
    }
}
