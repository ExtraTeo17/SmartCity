package web;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.abstractions.IMessageObjectMapper;
import web.abstractions.IWebConnector;
import web.message.MessageType;
import web.message.payloads.AbstractPayload;


class WebConnector implements IWebConnector {
    private static final Logger logger = LoggerFactory.getLogger(WebConnector.class);
    private final SocketServer socketServer;
    private final IMessageObjectMapper objectMapper;

    @Inject
    WebConnector(SocketServer socketServer, IMessageObjectMapper objectMapper) {
        this.socketServer = socketServer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void start() {
        socketServer.start();
    }

    @Override
    public void broadcastMessage(MessageType type, AbstractPayload payload) {
        var messageOpt = objectMapper.serialize(type, payload);
        if (messageOpt.isPresent()) {
            String serializedMessage = messageOpt.get();
            for (var bus : socketServer.getMessageBuses()) {
                bus.accept(serializedMessage);
            }
        }
        else {
            logger.warn("Message of type: " + type + "and payload: " + payload +
                    "won't be sent because of serialization error.");
        }
    }
}
