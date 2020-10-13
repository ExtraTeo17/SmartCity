package mocks;

import agents.utilities.LightColor;
import com.google.common.eventbus.EventBus;
import routing.core.Position;
import smartcity.ITimeProvider;
import smartcity.lights.core.Light;
import smartcity.lights.core.LightInfo;

import static org.mockito.Mockito.mock;

public class TestInstanceCreator {

    public static Light createLight() {
        var info = new LightInfo(1, 1, Position.of(1, 1), "1", "2");
        return new Light(info, LightColor.RED);
    }

    public static ITimeProvider createTimeProvider() {
        return mock(ITimeProvider.class);
    }

    public static EventBus createEventBus() {
        return new EventBus();
    }
}
