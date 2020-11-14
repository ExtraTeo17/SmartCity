package mocks;

import smartcity.lights.LightColor;
import com.google.common.eventbus.EventBus;
import routing.core.Position;
import smartcity.ITimeProvider;
import smartcity.lights.core.Light;
import smartcity.lights.core.LightInfo;
import smartcity.lights.core.SimpleLightGroup;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class TestInstanceCreator {

    public static Light createLight(LightColor color) {
        var info = new LightInfo(1, 1, Position.of(1, 1), "1", "2");
        return new Light(info, LightColor.RED);
    }

    public static SimpleLightGroup createLightGroup(LightColor color) {
        return new SimpleLightGroup(Arrays.asList(createLight(color), createLight(color)));
    }

    public static ITimeProvider createTimeProvider() {
        return mock(ITimeProvider.class);
    }

    public static EventBus createEventBus() {
        return new EventBus();
    }
}
