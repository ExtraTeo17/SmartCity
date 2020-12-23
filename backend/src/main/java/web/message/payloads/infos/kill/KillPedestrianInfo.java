package web.message.payloads.infos.kill;

import web.message.payloads.infos.kill.abstractions.AbstractKillInfoWithTravelData;

public class KillPedestrianInfo extends AbstractKillInfoWithTravelData {
    public KillPedestrianInfo(int id, int travelDistance, Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
