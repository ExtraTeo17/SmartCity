package web.abstractions;

import web.message.MessageType;
import web.message.payloads.AbstractPayload;

import java.util.Optional;

public interface IMessageObjectMapper {
    Optional<String> serialize(MessageType type, AbstractPayload payload);
}
