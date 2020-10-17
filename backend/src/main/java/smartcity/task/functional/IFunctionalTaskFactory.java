package smartcity.task.functional;

import com.google.inject.assistedinject.Assisted;
import smartcity.lights.core.Light;
import smartcity.task.data.ISwitchLightsContext;

import java.util.Collection;
import java.util.function.Function;

public interface IFunctionalTaskFactory {
    Function<ISwitchLightsContext, Integer> createLightSwitcher(@Assisted("managerId") int managerId,
                                                                @Assisted("extendTime") int extendTimeSeconds,
                                                                Collection<Light> lights);
}
