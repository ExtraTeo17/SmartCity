package web.abstractions;

/**
 * Used for activation management, when it is necessary to create entity earlier,
 * but some logic should be executed in future time.
 */
public interface IStartable {
    /**
     * Used to execute activation logic.
     */
    void start();
}
