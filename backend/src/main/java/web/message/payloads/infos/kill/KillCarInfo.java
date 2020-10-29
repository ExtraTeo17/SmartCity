package web.message.payloads.infos.kill;

import web.message.payloads.infos.kill.abstractions.AbstractKillInfoWithTravelData;

public class KillCarInfo extends AbstractKillInfoWithTravelData {
    public KillCarInfo(int id, int travelDistance, Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
