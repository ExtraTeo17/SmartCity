package osmproxy.buses.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import genesis.SharedModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import osmproxy.buses.models.ApiKeyValue;
import osmproxy.buses.models.ApiResult;
import osmproxy.buses.models.ApiValues;
import osmproxy.buses.models.TimetableRecord;
import testutils.FileLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static testutils.WarszawskieApiJsonCreator.createResultFromManyOrderedKeyValues;

@SuppressWarnings("SpellCheckingInspection")
class WarszawskieApiSerializerTests {
    private static ObjectMapper objectMapper;
    private static WarszawskieApiSerializer serializer;
    private static LocalDate dateNow;

    @BeforeAll
    static void setUpAll() {
        var injector = Guice.createInjector(new SharedModule());
        objectMapper = injector.getInstance(ObjectMapper.class);
        serializer = new WarszawskieApiSerializer(objectMapper);
        dateNow = LocalDate.now();
    }

    @ParameterizedTest
    @MethodSource("busDataProvider")
    void serializeTimetables_fullResponse_correctResult(String filename, int expectedCount,
                                                        LocalDateTime timeFirst,
                                                        LocalDateTime timeLast) {
        // Arrange
        var jsonString = FileLoader.getJsonString("warszawskieApi/" + filename);

        // Act
        var timetables = serializer.serializeTimetables(jsonString);

        // Assert
        assertEquals(expectedCount, timetables.size());

        var first = timetables.get(0);
        assertEquals(timeFirst, first.timeOnStop);

        var last = timetables.get(timetables.size() - 1);
        assertEquals(timeLast, last.timeOnStop);
    }

    static Stream<Arguments> busDataProvider() {
        return Stream.of(
                arguments("line_131_stop_7006_05.json", 72,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 12)),
                        LocalDateTime.of(dateNow, LocalTime.of(23, 12))),
                arguments("line_131_stop_7036_01.json", 72,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 13)),
                        LocalDateTime.of(dateNow, LocalTime.of(23, 13))),
                arguments("line_194_stop_4115_02.json", 66,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 27)),
                        LocalDateTime.of(dateNow.plusDays(1), LocalTime.of(0, 27))),
                arguments("line_194_stop_4124_02.json", 66,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 27)),
                        LocalDateTime.of(dateNow.plusDays(1), LocalTime.of(0, 27))),
                arguments("line_194_stop_4142_02.json", 66,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 28)),
                        LocalDateTime.of(dateNow.plusDays(1), LocalTime.of(0, 28))),
                arguments("line_519_stop_7006_05.json", 64,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 2)),
                        LocalDateTime.of(dateNow, LocalTime.of(23, 32))),
                arguments("line_523_stop_7006_02.json", 69,
                        LocalDateTime.of(dateNow, LocalTime.of(5, 9)),
                        LocalDateTime.of(dateNow, LocalTime.of(23, 39))),
                arguments("line_N25_stop_7006_07.json", 12,
                        LocalDateTime.of(dateNow, LocalTime.of(23, 21)),
                        LocalDateTime.of(dateNow.plusDays(1), LocalTime.of(4, 51)))
        );
    }

    @Test
    void serializeApiResult_onFullResponse() {
        var jsonString = FileLoader.getJsonString("warszawskieApi/line_194_stop_4115_02.json");
        var apiResult = createApiResultFromJson(jsonString);

        // Act
        var timetables = serializer.serializeApiResult(apiResult);

        // Assert
        assertEquals(66, timetables.size());

        assertTimetableIsEqual(timetables.get(0),
                "3", LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 27)),
                "PKP Gołąbki", "TP-GOL");

        assertTimetableIsEqual(timetables.get(timetables.size() - 1),
                "5", LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 27)),
                "PKP Gołąbki", "TP-GOL");
    }

    private ApiResult createApiResultFromJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, ApiResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    void serializeApiResult_onSingleRecord() {
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
                "3", LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 27)), "PKP Gołąbki", "TP-GOL");
    }

    @Test
    void serializeApiResult_onManyRecords() {
        // Arrange
        var apiResult = createApiResult(
                "symbol_1", "null",
                "symbol_2", "null",
                "brygada", "M4",
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
                "brygada", "192",
                "kierunek", "Międzylesie",
                "trasa", "TP-MIE",
                "czas", "28:51:00"
        );

        // Act
        var timetables = serializer.serializeApiResult(apiResult);

        // Assert
        assertEquals(3, timetables.size());

        assertTimetableIsEqual(timetables.get(0),
                "M4",
                LocalDateTime.of(dateNow, LocalTime.of(5, 27)),
                "PKP Gołąbki",
                "TP-GOL");

        assertTimetableIsEqual(timetables.get(1),
                "06",
                LocalDateTime.of(dateNow, LocalTime.of(18, 38)),
                "PKP Gołąbki",
                "TX-GOLV");

        assertTimetableIsEqual(timetables.get(2),
                "192",
                LocalDateTime.of(dateNow.plusDays(1), LocalTime.of(4, 51)),
                "Międzylesie",
                "TP-MIE");
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

    private void assertTimetableIsEqual(TimetableRecord timetable,
                                        String brigadeNr, LocalDateTime timeOnStop,
                                        String direction, String path) {
        assertEquals(brigadeNr, timetable.brigadeId);
        assertEquals(timeOnStop, timetable.timeOnStop);
        assertEquals(direction, timetable.direction);
        assertEquals(path, timetable.path);
    }

    @Test
    void serializeJsonString_onFullResponse() {
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

    @Test
    void serializeJsonString_onSingleRecord() {
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
    void serializeJsonString_onManyRecords() {
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
                "brygada", "M4",
                "kierunek", "Mi\\u0119dzylesie",
                "trasa", "TP-MIE",
                "czas", "28:21:00"
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
                "M4", "Międzylesie", "TP-MIE", "28:21:00");
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
        Assertions.assertTrue(pair.isPresent(), "Key " + key + "should be present");
        assertEquals(
                value, pair.get().value, "Value " + value + "should be set for " + key);
    }
}