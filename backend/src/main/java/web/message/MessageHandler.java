package web.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.SetZoneEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final EventBus eventBus;

    @Inject
    public MessageHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void Handle(String messageString) {
        MessageDto message = null;
        try {
            message = objectMapper.readValue(messageString, MessageDto.class);
        } catch (IOException e) {
            logger.error("Deserialization error", e);
            return;
        }

        Handle(message);
    }

    private void Handle(MessageDto message) {
        // TODO: Class hierarchy for appropriate message types
        // TODO: Event-based interaction with backend
        switch (message.type) {
            case SET_ZONE:
                // TODO: Based on message payload
                eventBus.post(new SetZoneEvent(52.23682, 21.01681, 600));
                break;
        }
    }
}
