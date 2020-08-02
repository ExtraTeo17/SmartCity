package agents.utils;

public enum LightColor {
    GREEN,
    YELLOW,
    RED;

    public LightColor next() {
        if (this == GREEN) {
            return LightColor.RED;
        }
        else if (this == RED) {
            return LightColor.GREEN;
        }
        else if (this == YELLOW) {
            return LightColor.YELLOW;
        }

        throw new IllegalArgumentException("Method was not updated after new field was added.");
    }
}
