package osmproxy.buses.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import genesis.SharedModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import osmproxy.buses.models.ApiKeyValue;
import osmproxy.buses.models.ApiResult;
import osmproxy.buses.models.ApiValues;
import osmproxy.buses.models.SingleTimetable;
import testutils.FileLoader;
import utilities.IterableJsonArray;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutils.WarszawskieApiJsonCreator.createResultFromManyOrderedKeyValues;

@SuppressWarnings("SpellCheckingInspection")
class WarszawskieApiSerializerTests {
    private static final JSONParser jsonParser = new JSONParser();
    private static WarszawskieApiSerializer serializer;

    @BeforeAll
    static void setUpAll() {
        var injector = Guice.createInjector(new SharedModule());
        serializer = new WarszawskieApiSerializer(injector.getInstance(ObjectMapper.class));
    }

    @Test
    void serializeTimetables() {
        // Arrange

        // Act

        // Assert
    }

    @Test
    void serializeApiResult_onFullResponse_shouldSucceed() {
        var jsonString = FileLoader.getJsonString("warszawskieApi/line_194_stop_4115_02.json");
        var apiResult = createApiResultFromJson(jsonString);

        // Act
        var timetables = serializer.serializeApiResult(apiResult);

        // Assert
        assertEquals(66, timetables.size());

        assertTimetableIsEqual(timetables.get(0),
                3, LocalTime.of(5, 27, 0), "PKP Gołąbki", "TP-GOL");

        assertTimetableIsEqual(timetables.get(timetables.size() - 1),
                5, LocalTime.of(0, 27, 0), "PKP Gołąbki", "TP-GOL");
    }

    private ApiResult createApiResultFromJson(String jsonString) {
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        var resultArray = (JSONArray) jsonObject.get("result");
        var orderedKeyValues = new ArrayList<String>(resultArray.size());
        for (var valuesArray : IterableJsonArray.of(jsonObject, "result")) {
            for (var keyValue : IterableJsonArray.of(valuesArray, "values")) {
                orderedKeyValues.add((String) keyValue.get("key"));
                orderedKeyValues.add((String) keyValue.get("value"));
            }
        }

        return createApiResult(orderedKeyValues.toArray(String[]::new));
    }

    @Test
    void serializeApiResult_onSingleRecord_shouldSucceed() {
        // Arrange
        var apiResult = createApiResult(
                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "3",
                "kierunek", "PKP Gołąbki",
                "trasa", "TP-GOL",
                "czas", "05:27:00"
        );

        // Act
        var timetables = serializer.serializeApiResult(apiResult);

        // Assert
        assertEquals(1, timetables.size());

        assertTimetableIsEqual(timetables.get(0),
                3, LocalTime.of(5, 27, 0), "PKP Gołąbki", "TP-GOL");
    }

    @Test
    void serializeApiResult_onManyRecords_shouldSucceed() {
        // Arrange
        var apiResult = createApiResult(
                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "3",
                "kierunek", "PKP Gołąbki",
                "trasa", "TP-GOL",
                "czas", "05:27:00",

                "symbol_2", "null",
                "symbol_1", "#",
                "brygada", "06",
                "kierunek", "PKP Gołąbki",
                "trasa", "TX-GOLV",
                "czas", "18:38:00",

                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "1",
                "kierunek", "PKP Gołąbki",
                "trasa", "TP-GOL",
                "czas", "24:28:00"
        );

        // Act
        var timetables = serializer.serializeApiResult(apiResult);

        // Assert
        assertEquals(3, timetables.size());

        assertTimetableIsEqual(timetables.get(0),
                3, LocalTime.of(5, 27, 0), "PKP Gołąbki", "TP-GOL");

        assertTimetableIsEqual(timetables.get(1),
                6, LocalTime.of(18, 38, 0), "PKP Gołąbki", "TX-GOLV");

        assertTimetableIsEqual(timetables.get(2),
                1, LocalTime.of(0, 28, 0), "PKP Gołąbki", "TP-GOL");
    }

    private ApiResult createApiResult(String... orderedKeyValues) {
        return createApiResult(6, orderedKeyValues);
    }

    private ApiResult createApiResult(int fieldsCount, String... orderedKeyValues) {
        int pairLength = fieldsCount * 2;
        int apiValuesCount = orderedKeyValues.length / pairLength;
        List<ApiValues> apiValuesList = new ArrayList<>(apiValuesCount);
        for (int valuesIter = 0; valuesIter < apiValuesCount; ++valuesIter) {
            int start = valuesIter * pairLength;
            int end = start + pairLength;
            var apiValues = createApiValuesFromSubArray(orderedKeyValues, start, end);
            apiValuesList.add(apiValues);
        }

        return new ApiResult(apiValuesList);
    }

    private ApiValues createApiValuesFromSubArray(String[] orderedKeyValues, int start, int end) {
        List<ApiKeyValue> apiKeyValues = new ArrayList<>(end - start);
        for (int i = start; i < end; i += 2) {
            apiKeyValues.add(new ApiKeyValue(orderedKeyValues[i], orderedKeyValues[i + 1]));
        }

        return new ApiValues(apiKeyValues);
    }

    private void assertTimetableIsEqual(SingleTimetable timetable,
                                        int brigadeNr, LocalTime timeOnStop,
                                        String direction, String path) {
        assertEquals(timetable.brigadeNr, brigadeNr);
        assertEquals(timetable.timeOnStop, timeOnStop);
        assertEquals(timetable.direction, direction);
        assertEquals(timetable.path, path);
    }

    @Test
    void serializeJsonString_onSingleRecord_shouldSucceed() {
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
        var apiResultOpt = serializer.serializeJsonString(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(1, apiValues.size());

        assertValuesAreEqual(apiValues.get(0).values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
    }

    @Test
    void serializeJsonString_onManyRecords_shouldSucceed() {
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
        var apiResultOpt = serializer.serializeJsonString(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(3, apiValues.size());

        assertValuesAreEqual(apiValues.get(0).values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
        assertValuesAreEqual(apiValues.get(1).values,
                "06", "PKP Gołąbki", "TX-GOLV", "18:38:00", "null", "#");
        assertValuesAreEqual(apiValues.get(2).values,
                "1", "PKP Gołąbki", "TP-GOL", "05:28:00");
    }

    @Test
    void serializeJsonString_onFullResponse_shouldSucceed() {
        // Arrange
        var jsonString = FileLoader.getJsonString("warszawskieApi/line_194_stop_4124_02.json");

        // Act
        var apiResultOpt = serializer.serializeJsonString(jsonString);

        // Assert
        Assertions.assertTrue(apiResultOpt.isPresent());

        var apiValues = apiResultOpt.get().apiValues;
        assertEquals(66, apiValues.size());

        var first = apiValues.get(0);
        assertValuesAreEqual(first.values,
                "3", "PKP Gołąbki", "TP-GOL", "05:27:00");
        var last = apiValues.get(apiValues.size() - 1);
        assertValuesAreEqual(last.values,
                "5", "PKP Gołąbki", "TP-GOL", "24:27:00");
    }

    private void assertValuesAreEqual(List<ApiKeyValue> keyValues, String brygada,
                                      String kierunek, String trasa, String czas) {
        assertValuesAreEqual(keyValues, brygada, kierunek, trasa, czas,
                "null", "null");
    }

    private void assertValuesAreEqual(List<ApiKeyValue> keyValues, String brygada,
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