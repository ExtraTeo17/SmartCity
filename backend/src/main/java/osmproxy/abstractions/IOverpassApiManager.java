package osmproxy.abstractions;

import java.net.HttpURLConnection;
import java.util.Optional;

public interface IOverpassApiManager {
    Optional<HttpURLConnection> sendRequest(String query);
}
