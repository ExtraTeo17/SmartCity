package smartcity.lights;

public enum LightColor {
    GREEN,
    YELLOW,
    RED;

    public LightColor next() {
        return switch (this) {
            case GREEN -> LightColor.RED;
            case YELLOW -> LightColor.YELLOW;
            case RED -> LightColor.GREEN;
        };
    }
}
