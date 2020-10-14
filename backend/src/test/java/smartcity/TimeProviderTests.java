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
        int waitTimeMillis = 300;
        int accuracyMillis = 10;

        // Act
        timeProvider.setSimulationStartTime(startTime);
        var realTime = System.nanoTime();
        trySleep(waitTimeMillis);
        realTime = (System.nanoTime() - realTime) / 1_000_000;
        var resultTime = timeProvider.getCurrentSimulationTime();

        // Assert
        var resultTimeDiff = ChronoUnit.MILLIS.between(startTime, resultTime);
        var min = TimeProvider.TIME_SCALE * (realTime - accuracyMillis);
        var max = TimeProvider.TIME_SCALE * (realTime + accuracyMillis);
        assertTrue(resultTimeDiff >= min &&
                        resultTimeDiff <= max,
                "Should be: [" + min + ", " + max + "], but is: " + resultTimeDiff + "[ms]");
    }
}