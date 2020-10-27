package web.message.payloads.infos.kill;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.infos.kill.abstractions.AbstractKillInfo;

public class HideTroublePointInfo extends AbstractKillInfo {
    public HideTroublePointInfo(int id) {
        super(id);
    }
}
