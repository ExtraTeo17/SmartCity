package smartcity.task.data;

/**
 * Helps to control switching of the lights
 */
public interface ISwitchLightsContext {
    boolean haveAlreadyExtended();

    void setAlreadyExtendedGreen(boolean value);
}
