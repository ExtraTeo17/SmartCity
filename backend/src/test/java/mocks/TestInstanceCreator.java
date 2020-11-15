package mocks;

import com.google.common.eventbus.EventBus;
import routing.core.Position;
import smartcity.ITimeProvider;
import smartcity.lights.LightColor;
import smartcity.lights.core.Light;
import smartcity.lights.core.LightInfo;
import smartcity.lights.core.SimpleLightGroup;
import utilities.Siblings;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInstanceCreator {

    public static Light createLight(LightColor color) {
        var info = new LightInfo(1, 1, Position.of(1, 1), "1", "2");
        return new Light(info, color);
    }

    public static SimpleLightGroup createLightGroup(LightColor color) {
        return new SimpleLightGroup(Arrays.asList(createLight(color), createLight(color)));
    }

    public static Siblings<SimpleLightGroup> createLights() {
        var a = createLightGroup(LightColor.GREEN);
        var b = createLightGroup(LightColor.RED);
        return Siblings.of(a, b);
    }

    public static ITimeProvider createTimeProvider() {
        var provider = mock(ITimeProvider.class);
        when(provider.getTimeScale()).thenReturn(1);

        return provider;
    }

    public static EventBus createEventBus() {
        return new EventBus();
    }
}
