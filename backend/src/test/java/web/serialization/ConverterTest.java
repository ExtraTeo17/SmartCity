package web.serialization;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import web.message.payloads.responses.Location;

class ConverterTest {

    @Test
    void convert_correctData_correctResult() {
        double lat = 52.00222;
        double lng = 21.001;
        GeoPosition geoPosition = new GeoPosition(lat, lng);

        Location result = Converter.convert(geoPosition);

        Assert.assertEquals(lat, result.latitude, 0);
        Assert.assertEquals(lng, result.longitude, 0);
    }
}