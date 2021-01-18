package osmproxy.routes;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.graphhopper.util.CmdArgs;
import genesis.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class RoutingModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(RoutingModule.class);

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

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
            var configResource = HighwayAccessor.class.getClassLoader().getResource("");
            if (configResource == null) {
                logger.error("Didn't find the loader resource");
                return new CmdArgs();
            }

            var mainPath = Paths.get(configResource.toURI()).toString();
            var configPath = Paths.get(mainPath, "graphHopper.properties");
            var dataReaderPath = Paths.get(mainPath, "mazowieckie-latest.osm.pbf");
            var args = new String[]{"config=" + configPath, "datareader.file=" + dataReaderPath};
            return CmdArgs.read(args);
        } catch (Exception e) {
            logger.error("Error in reading args", e);
        }

        return new CmdArgs();
    }
}
