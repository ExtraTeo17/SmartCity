package vehicles;

import java.time.LocalDateTime;

/**
 * Objects, for which time and distance will be measured
 */
public interface ITestable {
    LocalDateTime getStart();

    LocalDateTime getEnd();
}
