package web;

import web.message.MessageType;
import web.message.payloads.AbstractPayload;

interface IWebConnector extends IStartable {
    void broadcastMessage(MessageType type, AbstractPayload payload);
}
