package osmproxy.buses.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import osmproxy.buses.models.ApiKeyValue;
import testutils.FileLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutils.WarszawskieApiJsonCreator.createResultFromManyOrderedKeyValues;

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
    void serializeApiResult_onSingleRecord_shouldSucceed() {
        // Arrange
        var jsonString = createResultFromManyOrderedKeyValues(
                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "3",
                "kierunek", "PKP Go\\u0142\\u0105bki",
                "trasa", "TP-GOL",
                "czas", "05:27:00"
        );


        // Act
        var apiResultOpt = serializer.serializeApiResult(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(1, apiValues.size());

        assertValuesAreCorrect(apiValues.get(0).values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
    }

    @Test
    void serializeApiResult_onManyRecords_shouldSucceed() {
        // Arrange
        var jsonString = createResultFromManyOrderedKeyValues(
                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "3",
                "kierunek", "PKP Go\\u0142\\u0105bki",
                "trasa", "TP-GOL",
                "czas", "05:27:00",

                "symbol_2", "null",
                "symbol_1", "#",
                "brygada", "06",
                "kierunek", "PKP Go\\u0142\\u0105bki",
                "trasa", "TX-GOLV",
                "czas", "18:38:00",

                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "1",
                "kierunek", "PKP Go\\u0142\\u0105bki",
                "trasa", "TP-GOL",
                "czas", "05:28:00"
        );

        // Act
        var apiResultOpt = serializer.serializeApiResult(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(3, apiValues.size());

        assertValuesAreCorrect(apiValues.get(0).values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
        assertValuesAreCorrect(apiValues.get(1).values,
                "06", "PKP Gołąbki", "TX-GOLV", "18:38:00", "null", "#");
        assertValuesAreCorrect(apiValues.get(2).values,
                "1", "PKP Gołąbki", "TP-GOL", "05:28:00");
    }

    @Test
    void serializeApiResult_onFullResponse_shouldSucceed() {
        // Arrange
        var jsonString = FileLoader.getJsonString("warszawskieApi/line_194_stop_4124_02.json");

        // Act
        var apiResultOpt = serializer.serializeApiResult(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(66, apiValues.size());

        var first = apiValues.get(0);
        assertValuesAreCorrect(first.values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
        var last = apiValues.get(apiValues.size() - 1);
        assertValuesAreCorrect(last.values,
                "5", "PKP Gołąbki", "TP-GOL", "24:27:00");
    }

    private void assertValuesAreCorrect(List<ApiKeyValue> keyValues, String brygada,
                                        String kierunek, String trasa, String czas) {
        assertValuesAreCorrect(keyValues, brygada, kierunek, trasa, czas,
                "null", "null");
    }

    private void assertValuesAreCorrect(List<ApiKeyValue> keyValues, String brygada,
                                        String kierunek, String trasa, String czas,
                                        String... symbols) {
        assertEquals(6, keyValues.size());
        assertValuesAreEqual(keyValues, "symbol_2", symbols[0]);
        assertValuesAreEqual(keyValues, "symbol_1", symbols[1]);
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