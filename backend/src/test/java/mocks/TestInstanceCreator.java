package mocks;

import agents.utilities.LightColor;
import routing.core.Position;
import smartcity.lights.core.Light;
import smartcity.lights.core.LightInfo;

public class TestInstanceCreator {

    public static Light createLight() {
        var info = new LightInfo(1, 1, Position.of(1, 1), "1", "2");
        return new Light(info, LightColor.RED, 1);
    }
}
