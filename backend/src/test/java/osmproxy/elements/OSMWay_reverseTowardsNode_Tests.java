package osmproxy.elements;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class OSMWay_reverseTowardsNode_Tests {

    @ParameterizedTest
    @MethodSource("compatibleWaypointsProvider")
    void differentWays_correctResult(String testCaseName,
                                     OSMWaypoint[] waypointsA, OSMWaypoint[] waypointsB) {
        // Arrange
        var wayA = new OSMWay(1, Lists.newArrayList(waypointsA));
        var wayB = new OSMWay(2, Lists.newArrayList(waypointsB));

        // Act
        var result = wayA.orientateWith(wayB);

        // Assert
        Assertions.assertEquals(wayB.getWaypoint(-1).getOsmNodeRef(), result,
                testCaseName + ": Returned reference should be last in secondWay: " + testCaseName);
        Assertions.assertEquals(wayA.getWaypoint(-1), wayB.getWaypoint(0),
                testCaseName + ": LastWaypoint of first way should be equal to firstWaypoint on the second: ");
    }

    @ParameterizedTest
    @MethodSource("incompatibleWaypointsProvider")
    void unconnectedWays_throwsException(String testCaseName,
                                         OSMWaypoint[] waypointsA, OSMWaypoint[] waypointsB) {
        // Arrange
        var wayA = new OSMWay(1, Lists.newArrayList(waypointsA));
        var wayB = new OSMWay(2, Lists.newArrayList(waypointsB));

        // Act & Assert
        Assertions.assertThrows(IllegalStateException.class, () -> wayA.orientateWith(wayB));
    }

    static Stream<Arguments> compatibleWaypointsProvider() {
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
                        "Reverse first",
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
                        "Reverse both",
                        new OSMWaypoint[]{
                                createTestWaypoint("5"),
                                createTestWaypoint("2"),
                                createTestWaypoint("3")},
                        new OSMWaypoint[]{
                                createTestWaypoint("7"),
                                createTestWaypoint("6"),
                                createTestWaypoint("3")}
                )
        );
    }

    static Stream<Arguments> incompatibleWaypointsProvider() {
        return Stream.of(
                arguments(
                        "Node in middle in the first",
                        new OSMWaypoint[]{
                                createTestWaypoint("5"),
                                createTestWaypoint("1"),
                                createTestWaypoint("4")},
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("6"),
                                createTestWaypoint("2")}
                ),
                arguments(
                        "Nodes in middle in the second",
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("12"),
                                createTestWaypoint("4"),
                                createTestWaypoint("5")},
                        new OSMWaypoint[]{
                                createTestWaypoint("50"),
                                createTestWaypoint("1"),
                                createTestWaypoint("2"),
                                createTestWaypoint("20")}
                ),
                arguments(
                        "No common nodes",
                        new OSMWaypoint[]{
                                createTestWaypoint("1"),
                                createTestWaypoint("2"),
                                createTestWaypoint("3")},
                        new OSMWaypoint[]{
                                createTestWaypoint("5"),
                                createTestWaypoint("7"),
                                createTestWaypoint("8")}
                )
        );
    }

    private static OSMWaypoint createTestWaypoint(String nodeRef) {
        return new OSMWaypoint(nodeRef, 1, 1);
    }
}