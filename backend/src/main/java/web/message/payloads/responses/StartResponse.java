package web.message.payloads.responses;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import web.message.payloads.AbstractPayload;

@JsonSerialize
public class StartResponse extends AbstractPayload {
    public StartResponse() {
    }
}
