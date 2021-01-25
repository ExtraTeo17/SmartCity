package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.DebugEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.StaticConfig;
import web.message.MessageDto;
import web.message.payloads.requests.PrepareSimulationRequest;
import web.message.payloads.requests.StartSimulationRequest;
import web.serialization.Converter;

import java.io.IOException;
import java.util.Optional;

/**
 * Used for handling messages from frontend. <br/>
 * Responsible for deserialization, type conversion and posting events
 * corresponding to each of requests. <br/>
 * Handles: <br/>
 * <ul>
 *     <li>{@link web.message.MessageType#START_SIMULATION_REQUEST}</li>
 *     <li>{@link web.message.MessageType#PREPARE_SIMULATION_REQUEST}</li>
 *     <li>{@link web.message.MessageType#DEBUG_REQUEST}</li>
 * </ul>
 */
class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final ObjectMapper objectMapper;
    private final EventBus eventBus;

    @Inject
    MessageHandler(ObjectMapper objectMapper, EventBus eventBus) {
        this.objectMapper = objectMapper;
        this.eventBus = eventBus;
    }

    void handle(String messageString) {
        logger.info("Handling message: " + messageString);

        var message = tryDeserialize(messageString, MessageDto.class);
        if (message.isEmpty()) {
            return;
        }

        handle(message.get());
    }

    private void handle(MessageDto message) {
        switch (message.type) {
            case PREPARE_SIMULATION_REQUEST -> {
                var payload = tryDeserialize(message.payload, PrepareSimulationRequest.class);
                if (payload.isPresent()) {
                    var pVal = payload.get();
                    var event = Converter.convert(pVal);
                    eventBus.post(event);
                }
            }
            case START_SIMULATION_REQUEST -> {
                var payload = tryDeserialize(message.payload, StartSimulationRequest.class);
                if (payload.isPresent()) {
                    var pVal = payload.get();

                    var event = Converter.convert(pVal);
                    logger.info("Starting simulation request with time: " + event.startTime);
                    eventBus.post(event);
                }
            }
            case DEBUG_REQUEST -> {
                if (StaticConfig.DEBUG) {
                    logger.info("Debug request occurred");
                    eventBus.post(new DebugEvent());
                }
            }
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
