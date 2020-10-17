package smartcity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var updateTime = timeProvider.getUpdateTimeTask(0);
        for (int i = 0; i < waitTimeMillis / TimeProvider.MS_PER_TICK; ++i) {
            updateTime.run();
        }
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