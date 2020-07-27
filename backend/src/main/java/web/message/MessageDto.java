package web.message;

import java.io.Serializable;

// TODO: needed (serializable)?
public class MessageDto implements Serializable {
    public MessageType type;
    public String payload;
}
