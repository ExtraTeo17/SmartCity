package web;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.message.MessageType;
import web.message.payloads.AbstractPayload;
import web.serialization.MessageObjectMapper;


public class WebManager implements IWebManager {
    private static final Logger logger = LoggerFactory.getLogger(WebManager.class);
    private final WebServer webServer;
    private final MessageObjectMapper objectMapper;

    @Inject
    public WebManager(WebServer webServer, MessageObjectMapper objectMapper) {
        this.webServer = webServer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void start() {
        webServer.start();
    }

    @Override
    public void broadcastMessage(MessageType type, AbstractPayload payload) {
        var messageOpt = objectMapper.serialize(type, payload);
        if (messageOpt.isPresent()) {
            String serializedMessage = messageOpt.get();
            for (var bus : webServer.getMessageBuses()) {
                bus.accept(serializedMessage);
            }
        }
        else {
            logger.warn("Message of type: " + type + "and payload: " + payload +
                    "won't be sent because of serialization error.");
        }
    }
}
