package web;

import web.message.MessageType;
import web.message.payloads.AbstractPayload;

public interface IWebManager {
    void start();
    void broadcastMessage(MessageType type, AbstractPayload payload);
}
