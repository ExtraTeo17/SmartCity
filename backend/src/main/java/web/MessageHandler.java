package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.SetZoneEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.message.MessageDto;
import web.message.payloads.requests.SetZoneRequest;

import java.io.IOException;
import java.util.Optional;

public class MessageHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final EventBus eventBus;

    @Inject
    public MessageHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void Handle(String messageString) {
        logger.info("Handling message: " + messageString);

        var message = tryDeserialize(messageString, MessageDto.class);
        if (message.isEmpty()) {
            return;
        }

        Handle(message.get());
    }

    private void Handle(MessageDto message) {
        switch (message.type) {
            case SET_ZONE_REQUEST:
                var payload = tryDeserialize(message.payload, SetZoneRequest.class);
                if (payload.isPresent()) {
                    var pVal = payload.get();
                    // TODO: Mapper?
                    eventBus.post(new SetZoneEvent(pVal.latitude, pVal.longitude, pVal.radius));
                }
                break;
        }
    }

    private <T> Optional<T> tryDeserialize(String json, Class<T> type) {
        T result;
        try {
            result = objectMapper.readValue(json, type);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Deserialization error", e);
            return Optional.empty();
        }

        return Optional.of(result);
    }
}
