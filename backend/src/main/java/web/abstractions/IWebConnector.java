package web.abstractions;

import web.message.MessageType;
import web.message.payloads.AbstractPayload;

public interface IWebConnector extends IStartable {
    void broadcastMessage(MessageType type, AbstractPayload payload);
}
