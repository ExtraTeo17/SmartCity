package osmproxy.buses.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class WrappingDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    /**
     * @return Date parsed from HH:mm:ss format, where HH >= 0, HH < 30
     */
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        String time = jsonParser.getText();
        String[] timeParts = time.split(":");
        if (timeParts.length != 3) {
            throw new RuntimeException("Invalid time format: '" + time + "', partsNum=" + timeParts.length);
        }

        var date = LocalDate.now();
        int hours = Integer.parseInt(timeParts[0]);
        if (hours > 23) {
            date = date.plusDays(1);
            hours -= 24;
        }
        int minutes = Integer.parseInt(timeParts[1]);

        return LocalDateTime.of(date, LocalTime.of(hours, minutes));
    }
}
