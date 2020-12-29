package osmproxy;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import testutils.ReflectionHelper;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class OverpassApiManagerTests {
    private OverpassApiManager overpassApiManager;
    private EventBus eventBus;

    @BeforeEach
    void beforeEach() {
        eventBus = mock(EventBus.class);
        overpassApiManager = new OverpassApiManager(new String[]{"A", "B", "C", "D"},
                eventBus);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            OverpassApiManager.API_SWITCH_NOTIFY_COUNT - 1,
            OverpassApiManager.API_SWITCH_NOTIFY_COUNT,
            OverpassApiManager.API_SWITCH_NOTIFY_COUNT + 1,
    })
    void switchApi_onSwitch_shouldNotifyIfLessOrEqualThanThreshold(int switchCount) {
        // Arrange
        AtomicBoolean notified = new AtomicBoolean();
        doAnswer(invocation -> {
            notified.set(true);
            return null;
        }).when(eventBus).post(any(Object.class));

        // Act
        for (int i = 0; i < switchCount; ++i) {
            this.overpassApiManager.switchApi();
        }

        // Assert
        boolean shouldNotify = switchCount > OverpassApiManager.API_SWITCH_NOTIFY_COUNT;
        assertEquals(shouldNotify, notified.get());
    }

    @Test
    void switchApi_onSwitchMoreThanThreshold_shouldNotifyOnlyOnce() {
        // Arrange
        var notifyCount = new AtomicInteger();
        doAnswer(invocation -> {
            notifyCount.addAndGet(1);
            return null;
        }).when(eventBus).post(any(Object.class));

        // Act
        var limit = OverpassApiManager.API_SWITCH_NOTIFY_COUNT + 5;
        for (int i = 0; i < limit; ++i) {
            this.overpassApiManager.switchApi();
        }

        // Assert
        assertEquals(1, notifyCount.get(), "Should notify only once");
    }

    @Test
    void switchApi_onCrossingThresholdManyTimes_shouldNotifyManyTimes() {
        // Arrange
        var notifyCount = new AtomicInteger();
        doAnswer(invocation -> {
            notifyCount.addAndGet(1);
            return null;
        }).when(eventBus).post(any(Object.class));
        Queue<LocalDateTime> queue = ReflectionHelper.getField("switchTimeQueue", overpassApiManager);

        // Act
        var limit = OverpassApiManager.API_SWITCH_NOTIFY_COUNT + 1;
        for (int i = 0; i < limit; ++i) {
            this.overpassApiManager.switchApi();
        }

        queue.poll();
        this.overpassApiManager.switchApi();

        queue.poll();
        this.overpassApiManager.switchApi();

        // Assert
        assertEquals(3, notifyCount.get(), "Should notify many times");
    }
}