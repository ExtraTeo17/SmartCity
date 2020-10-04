package smartcity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.ThreadHelper.trySleep;

class TimeProviderTests {

    @Test
    void getCurrentSimulationTime_happyPath() {
        // Arrange
        var startTime = LocalDateTime.of(LocalDate.now().minusDays(1),
                LocalTime.of(10, 10, 10));
        var timeProvider = new TimeProvider();
        int waitTimeMillis = 200;
        int accuracyMillis = 15;

        // Act
        timeProvider.setSimulationStartTime(startTime);
        trySleep(waitTimeMillis);
        var resultTime = timeProvider.getCurrentSimulationTime();

        // Assert
        var resultTimeDiff = ChronoUnit.MILLIS.between(startTime, resultTime);
        var min = TimeProvider.TIME_SCALE * (waitTimeMillis - accuracyMillis);
        var max = TimeProvider.TIME_SCALE * (waitTimeMillis + accuracyMillis);
        assertTrue(resultTimeDiff >= min &&
                        resultTimeDiff <= max,
                "Should be: [" + min + ", " + max + "], but is: " + resultTimeDiff + "[ms]");
    }
}