package web.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.abstractions.IMessageObjectMapper;
import web.message.MessageDto;
import web.message.MessageType;
import web.message.payloads.AbstractPayload;

import java.util.Optional;

class MessageObjectMapper implements IMessageObjectMapper {
    private static final Logger logger = LoggerFactory.getLogger(MessageObjectMapper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> serialize(MessageType type, AbstractPayload payload) {
        String serializedPayload;
        try {
            serializedPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.warn("Error serializing payload", e);
            return Optional.empty();
        }

        MessageDto message = new MessageDto(type, serializedPayload);
        String serializedMessage;
        try {
            serializedMessage = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.warn("Error serializing message", e);
            return Optional.empty();
        }

        return Optional.of(serializedMessage);
    }
}
