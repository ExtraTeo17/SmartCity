package web.serialization;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import routing.core.IGeoPosition;
import routing.core.Position;
import web.message.payloads.models.Location;

class ConverterTest {

    @Test
    void convert_happyPath() {
        double lat = 52.00222;
        double lng = 21.001;
        IGeoPosition geoPosition = Position.of(lat, lng);

        Location result = Converter.convert(geoPosition);

        Assert.assertEquals(lat, result.latitude, 0);
        Assert.assertEquals(lng, result.longitude, 0);
    }
}