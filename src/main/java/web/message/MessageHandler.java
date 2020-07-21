package web.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public static void Handle(String messageString) {
        MessageDto message = null;
        try {
            message = objectMapper.readValue(messageString, MessageDto.class);
        } catch (IOException e) {
            logger.error("Deserialization error", e);
            return;
        }

        Handle(message);
    }

    private static void Handle(MessageDto message) {
        // TODO: Class hierarchy for appropriate message types
        // TODO: Event-based interaction with backend
        switch (message.type) {
        }
    }
}
