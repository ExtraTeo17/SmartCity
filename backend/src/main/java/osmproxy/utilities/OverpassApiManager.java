package osmproxy.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.IOverpassApiManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OverpassApiManager implements IOverpassApiManager {
    private static final Logger logger = LoggerFactory.getLogger(OverpassApiManager.class);

    private static final String OVERPASS_API = "https://lz4.overpass-api.de/api/interpreter";
    private static final String ALTERNATE_OVERPASS_API_1 = "https://z.overpass-api.de/api/interpreter";
    private static final String ALTERNATE_OVERPASS_API_2 = "https://overpass.kumi.systems/api/interpreter";
    private static final String ALTERNATE_OVERPASS_API_3 = "https://overpass.openstreetmap.ru/api/interpreter";

    private String CURRENT_API = ALTERNATE_OVERPASS_API_3;

    @Override
    public Optional<HttpURLConnection> sendRequest(String query) {
        HttpURLConnection connection = sendRequest(CURRENT_API, query);
        try {
            int responseCode = connection.getResponseCode();
            while (responseCode == 429 || responseCode == 504) {
                logger.warn("Current API: " + CURRENT_API + " is overloaded with our requests.");
                switchApi();
                connection = sendRequest(CURRENT_API, query);
                responseCode = connection.getResponseCode();
            }
        } catch (IOException e) {
            logger.error("Error getting response code from connection", e);
            return Optional.empty();
        }

        return Optional.of(connection);
    }

    private static HttpURLConnection sendRequest(String apiAddress, String query) {
        HttpURLConnection connection = getConnection(apiAddress);
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

    private void switchApi() {
        switch (CURRENT_API) {
            case OVERPASS_API -> CURRENT_API = ALTERNATE_OVERPASS_API_1;
            case ALTERNATE_OVERPASS_API_1 -> CURRENT_API = ALTERNATE_OVERPASS_API_2;
            case ALTERNATE_OVERPASS_API_2 -> CURRENT_API = ALTERNATE_OVERPASS_API_3;
            case ALTERNATE_OVERPASS_API_3 -> CURRENT_API = OVERPASS_API;
        }
        logger.info("Switching to " + CURRENT_API);
    }

    private static HttpURLConnection getConnection(String apiAddress) {
        URL url;
        try {
            url = new URL(apiAddress);
        } catch (MalformedURLException e) {
            logger.error("Error creating url: " + apiAddress);
            throw new RuntimeException(e);
        }

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("Error opening connection to " + apiAddress);
            throw new RuntimeException(e);
        }
    }
}
