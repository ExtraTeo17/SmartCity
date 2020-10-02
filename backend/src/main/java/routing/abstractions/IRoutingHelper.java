package routing.abstractions;

import routing.core.IGeoPosition;

public interface IRoutingHelper {
    IGeoPosition generateRandomOffset(int radius);
}
