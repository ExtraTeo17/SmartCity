package smartcity.task.functional;

import smartcity.lights.core.Light;
import smartcity.task.data.ISwitchLightsContext;

import java.util.Collection;
import java.util.function.Function;

public interface IFunctionalTaskFactory {
    Function<ISwitchLightsContext, Integer> createLightSwitcher(int extendTimeSeconds,
                                                                Collection<Light> lights);
}
