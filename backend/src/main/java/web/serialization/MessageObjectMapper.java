package web.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.abstractions.IMessageObjectMapper;
import web.message.MessageDto;
import web.message.MessageType;
import web.message.payloads.AbstractPayload;

import java.util.Optional;

class MessageObjectMapper implements IMessageObjectMapper {
    private static final Logger logger = LoggerFactory.getLogger(MessageObjectMapper.class);
    private final ObjectWriter objectWriter;

    @Inject
    MessageObjectMapper(ObjectWriter objectWriter) {
        this.objectWriter = objectWriter;
    }

    @Override
    public Optional<String> serialize(MessageType type, AbstractPayload payload) {
        String serializedPayload;
        try {
            serializedPayload = objectWriter.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.warn("Error serializing payload", e);
            return Optional.empty();
        }

        MessageDto message = new MessageDto(type, serializedPayload);
        String serializedMessage;
        try {
            serializedMessage = objectWriter.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.warn("Error serializing message", e);
            return Optional.empty();
        }

        return Optional.of(serializedMessage);
    }
}
