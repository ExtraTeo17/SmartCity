package web.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public static void Handle(String messageString) {
        Message message = null;
        try {
            message = MessageHandler.objectMapper.readValue(messageString, Message.class);
        } catch (IOException e) {
            MessageHandler.logger.error("Deserialization error", e);
            return;
        }

        MessageHandler.Handle(message);
    }

    private static void Handle(Message message) {
        // TODO: Class hierarchy for appropriate message types
        // TODO: Event-based interaction with backend
        switch (message.type) {
        }
    }
}
