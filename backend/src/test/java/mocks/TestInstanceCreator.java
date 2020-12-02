package mocks;

import com.google.common.eventbus.EventBus;
import routing.core.Position;
import smartcity.ITimeProvider;
import smartcity.lights.LightColor;
import smartcity.lights.core.Light;
import smartcity.lights.core.SimpleLightGroup;
import smartcity.lights.core.data.LightInfo;
import utilities.Siblings;

import java.util.Arrays;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInstanceCreator {
    private static final Random random = new Random();

    private static Light createLight(LightColor color) {
        var info = new LightInfo(random.nextInt(), random.nextInt(),
                Position.of(90 * random.nextDouble(), 80 * random.nextDouble()), "1", "2");
        return new Light(info, color);
    }

    public static SimpleLightGroup createLightGroup(LightColor color) {
        return new SimpleLightGroup(Arrays.asList(createLight(color), createLight(color)));
    }


    public static Siblings<SimpleLightGroup> createLights() {
        return createLights(true);
    }

    public static Siblings<SimpleLightGroup> createLights(boolean firstGreen) {
        var a = createLightGroup(LightColor.GREEN);
        var b = createLightGroup(LightColor.RED);

        return firstGreen ? Siblings.of(a, b) : Siblings.of(b, a);
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
