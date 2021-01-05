package smartcity.task.functional;

import com.google.inject.assistedinject.Assisted;
import smartcity.lights.core.SimpleLightGroup;
import smartcity.task.data.ISwitchLightsContext;
import utilities.Siblings;

import java.util.function.Function;

/**
 * Forces implementing functionality
 */
public interface IFunctionalTaskFactory {
    Function<ISwitchLightsContext, Integer> createLightSwitcher(@Assisted("managerId") int managerId,
                                                                @Assisted("extendTime") int extendTimeSeconds,
                                                                @Assisted Siblings<SimpleLightGroup> lights);
}
