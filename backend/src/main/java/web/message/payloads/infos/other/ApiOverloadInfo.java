package web.message.payloads.infos.other;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import web.message.payloads.AbstractPayload;

@JsonSerialize
public class ApiOverloadInfo extends AbstractPayload {
    public ApiOverloadInfo() {
    }
}
