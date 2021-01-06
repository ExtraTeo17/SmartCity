package web.abstractions;

import web.message.MessageType;
import web.message.payloads.AbstractPayload;

import java.util.Optional;

/**
 * Used for message serialization
 */
public interface IMessageObjectMapper {
    Optional<String> serialize(MessageType type, AbstractPayload payload);
}
