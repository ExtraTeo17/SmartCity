package osmproxy.buses.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import osmproxy.buses.models.ApiKeyValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
class WarszawskieApiSerializerTests {
    private static WarszawskieApiSerializer serializer;

    @BeforeAll
    static void setUpAll() {
        var objectMapper = new ObjectMapper();
        serializer = new WarszawskieApiSerializer(objectMapper);
    }

    @Test
    void serializeTimetables() {
        // Arrange

        // Act

        // Assert
    }

    @Test
    void serializeApiResult_onTwoRecords_shouldSucceed() {
        // Arrange
        var jsonString = "{\"result\":[" +
                "{\"values\":[" +
                "{\"value\":\"null\",\"key\":\"symbol_2\"}," +
                "{\"value\":\"null\",\"key\":\"symbol_1\"}," +
                "{\"value\":\"3\",\"key\":\"brygada\"}," +
                "{\"value\":\"PKP Go\\u0142\\u0105bki\",\"key\":\"kierunek\"}," +
                "{\"value\":\"TP-GOL\",\"key\":\"trasa\"}," +
                "{\"value\":\"05:27:00\",\"key\":\"czas\"}" +
                "]}," +
                "{\"values\":[" +
                "{\"value\":\"null\",\"key\":\"symbol_2\"}," +
                "{\"value\":\"null\",\"key\":\"symbol_1\"}," +
                "{\"value\":\"1\",\"key\":\"brygada\"}," +
                "{\"value\":\"PKP Go\\u0142\\u0105bki\",\"key\":\"kierunek\"}," +
                "{\"value\":\"TP-GOL\",\"key\":\"trasa\"}," +
                "{\"value\":\"05:57:00\",\"key\":\"czas\"}" +
                "]}" +
                "]}";

        // Act
        var apiResultOpt = serializer.serializeApiResult(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(2, apiValues.size());

        assertValuesAreCorrect(apiValues.get(0).values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
        assertValuesAreCorrect(apiValues.get(1).values,
                "1", "PKP Gołąbki", "TP-GOL", "05:57:00");
    }

    private void assertValuesAreCorrect(List<ApiKeyValue> keyValues, String brygada,
                                        String kierunek, String trasa, String czas) {
        assertEquals(6, keyValues.size());
        assertValuesAreEqual(keyValues, "symbol_2", "null");
        assertValuesAreEqual(keyValues, "symbol_1", "null");
        assertValuesAreEqual(keyValues, "brygada", brygada);
        assertValuesAreEqual(keyValues, "kierunek", kierunek);
        assertValuesAreEqual(keyValues, "trasa", trasa);
        assertValuesAreEqual(keyValues, "czas", czas);
    }

    private void assertValuesAreEqual(List<ApiKeyValue> values, String key, String value) {
        var pair = values.stream().filter(keyValue -> keyValue.key.equals(key)).findFirst();
        Assert.assertTrue("Key " + key + "should be present", pair.isPresent());
        Assert.assertEquals("Value " + value + "should be set for " + key,
                value, pair.get().value);
    }
}