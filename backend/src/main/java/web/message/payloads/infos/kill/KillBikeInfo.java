package web.message.payloads.infos.kill;

import web.message.payloads.infos.kill.abstractions.AbstractKillInfoWithTravelData;

public class KillBikeInfo extends AbstractKillInfoWithTravelData {
    public KillBikeInfo(int id, int travelDistance, Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
