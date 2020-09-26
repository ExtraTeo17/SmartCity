package osmproxy.elements;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class OSMWay_orientateWith_Tests {

    @ParameterizedTest
    @MethodSource("waypointsProvider")
    void differentWays_correctResult(String testCaseName,
                                     OSMWaypoint[] waypointsA, OSMWaypoint[] waypointsB) {
        // Arrange
        var wayA = new OSMWay(1, Lists.newArrayList(waypointsA));
        var wayB = new OSMWay(2, Lists.newArrayList(waypointsB));

        // Act
        var resultOpt = wayB.reverseTowardsNode(wayA.getEnd());

        // Assert
        Assertions.assertArrayEquals(waypointsA, wayA.getWaypoints().toArray(new OSMWaypoint[0]),
                testCaseName + ": Waypoints order in first way should not change");

        if (resultOpt.isPresent()) {
            Assertions.assertEquals(wayB.getWaypoint(-1).getOsmNodeRef(), resultOpt.get(),
                    testCaseName + ": Returned reference should be last in secondWay: " + testCaseName);
            Assertions.assertEquals(wayA.getWaypoint(-1), wayB.getWaypoint(0),
                    testCaseName + ": LastWaypoint of first way should be equal to firstWaypoint on the second: ");
        }
        else {
            Assertions.assertNotEquals(wayA.getWaypoint(-1), wayB.getWaypoint(0),
                    testCaseName + ": Compatible ways should not return empty result: " + testCaseName);
            Assertions.assertArrayEquals(waypointsB, wayB.getWaypoints().toArray(new OSMWaypoint[0]),
                    testCaseName + ": Waypoints order in second way should not change");
        }
    }

    static Stream<Arguments> waypointsProvider() {
        return Stream.of(
                arguments(
                        "No reverse",
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("2"),
                                createTestWaypoint("3"),
                                createTestWaypoint("4")},
                        new OSMWaypoint[]{
                                createTestWaypoint("4"),
                                createTestWaypoint("5"),
                                createTestWaypoint("6"),
                                createTestWaypoint("7")}
                ),
                arguments(
                        "Reverse second",
                        new OSMWaypoint[]{
                                createTestWaypoint("4"),
                                createTestWaypoint("5"),
                                createTestWaypoint("10"),
                                createTestWaypoint("1973")},
                        new OSMWaypoint[]{
                                createTestWaypoint("12"),
                                createTestWaypoint("15"),
                                createTestWaypoint("16"),
                                createTestWaypoint("1973")}
                ),
                arguments(
                        "Incompatible unless first reversed",
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("2"),
                                createTestWaypoint("3"),
                                createTestWaypoint("4")},
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("5"),
                                createTestWaypoint("6"),
                                createTestWaypoint("7")}
                ),
                arguments(
                        "Incompatible",
                        new OSMWaypoint[]{
                                createTestWaypoint("5"),
                                createTestWaypoint("2"),
                                createTestWaypoint("1"),
                                createTestWaypoint("4")},
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("7"),
                                createTestWaypoint("6"),
                                createTestWaypoint("2")}
                )
        );
    }

    private static OSMWaypoint createTestWaypoint(String nodeRef) {
        return new OSMWaypoint(nodeRef, 1, 1);
    }
}