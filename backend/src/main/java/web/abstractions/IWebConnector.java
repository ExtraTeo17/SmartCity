package web.abstractions;

import org.jetbrains.annotations.NotNull;
import web.message.MessageType;
import web.message.payloads.AbstractPayload;

/**
 * Used for message-related interaction
 */
public interface IWebConnector extends IStartable {
    /**
     * Sends message to all connected sockets
     * @param type - type of message
     * @param payload - data of message, may be empty class
     */
    void broadcastMessage(@NotNull MessageType type, @NotNull AbstractPayload payload);
}
