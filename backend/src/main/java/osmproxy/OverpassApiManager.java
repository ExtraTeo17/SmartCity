package osmproxy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.inject.name.Named;
import events.web.ApiOverloadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.IOverpassApiManager;

import javax.inject.Inject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class OverpassApiManager implements IOverpassApiManager {
    private static final Logger logger = LoggerFactory.getLogger(OverpassApiManager.class);

    public static final int API_SWITCH_NOTIFY_COUNT = 4;
    public static final int API_SWITCH_NOTIFY_TIME_SEC = 30;

    private final EventBus eventBus;
    private final String[] overpassApis;

    private final Queue<LocalDateTime> switchTimeQueue;
    private int currentApiIndex;

    @Inject
    public OverpassApiManager(@Named("OVERPASS_APIS") String[] overpassApis,
                              EventBus eventBus) {
        this.overpassApis = overpassApis;
        this.eventBus = eventBus;

        this.switchTimeQueue = new LinkedList<>();
        this.currentApiIndex = 1;
        logger.info("Starting with: " + getCurrentApi());
    }

    private String getCurrentApi() {
        return overpassApis[currentApiIndex];
    }

    @Override
    public Optional<HttpURLConnection> sendRequest(String query) {
        HttpURLConnection connection = trySendRequest(query);
        try {
            int responseCode = connection.getResponseCode();
            while (responseCode == 429 || responseCode == 504) {
                logger.warn("Current API: " + getCurrentApi() + " is overloaded with our requests.");
                switchApi();
                connection = trySendRequest(query);
                responseCode = connection.getResponseCode();
            }
        } catch (IOException e) {
            logger.error("Error getting response code from connection", e);
            return Optional.empty();
        }

        return Optional.of(connection);
    }

    private HttpURLConnection trySendRequest(String query) {
        HttpURLConnection connection = getConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try {
            DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            printout.flush();
            printout.close();
        } catch (IOException e) {
            logger.error("Error sending data to connection", e);
            throw new RuntimeException(e);
        }

        return connection;
    }

    private HttpURLConnection getConnection() {
        URL url;
        try {
            url = new URL(getCurrentApi());
        } catch (MalformedURLException e) {
            logger.error("Error creating url: " + getCurrentApi());
            throw new RuntimeException(e);
        }

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("Error opening connection to " + getCurrentApi());
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    void switchApi() {
        currentApiIndex = (currentApiIndex + 1) % overpassApis.length;
        logger.info("Switching to " + getCurrentApi());
        enqueueSwitchTime();
    }

    private void enqueueSwitchTime() {
        int initSize = switchTimeQueue.size();
        var timeNow = LocalDateTime.now();
        var timeNowThreshold = timeNow.minusSeconds(API_SWITCH_NOTIFY_TIME_SEC);
        switchTimeQueue.add(timeNow);
        while (switchTimeQueue.size() > 0 && switchTimeQueue.peek().isBefore(timeNowThreshold)) {
            switchTimeQueue.poll();
        }

        if (initSize <= API_SWITCH_NOTIFY_COUNT &&
                switchTimeQueue.size() > API_SWITCH_NOTIFY_COUNT) {
            eventBus.post(new ApiOverloadedEvent());
        }
        logger.info("Switched " + switchTimeQueue.size() + " times in the last " +
                API_SWITCH_NOTIFY_TIME_SEC + " seconds.");
    }
}
