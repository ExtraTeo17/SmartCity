package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.PrepareSimulationEvent;
import events.web.StartSimulationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.TimeProvider;
import web.message.MessageDto;
import web.message.payloads.requests.PrepareSimulationRequest;
import web.message.payloads.requests.StartSimulationRequest;

import java.io.IOException;
import java.sql.Time;
import java.util.Optional;

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
                    eventBus.post(new PrepareSimulationEvent(pVal.latitude, pVal.longitude, pVal.radius,
                            pVal.generatePedestrians));
                }
            }
            case START_SIMULATION_REQUEST -> {
                var payload = tryDeserialize(message.payload, StartSimulationRequest.class);
                if (payload.isPresent()) {
                    var pVal = payload.get();

                    var timeLocal = TimeProvider.convertFromUtcToLocal(pVal.startTime).toLocalDateTime();
                    logger.info("Starting simulation request with time: " + timeLocal);
                    eventBus.post(new StartSimulationEvent(pVal.carsNum, pVal.testCarId, pVal.generateCars,
                            pVal.generateTroublePoints, timeLocal));
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
