package osmproxy.elements;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import routing.core.Position;
import testutils.FileLoader;
import utilities.IterableNodeList;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(Lifecycle.PER_CLASS)
class OSMWay_findClosestNodeRefTo_Tests {

    private OSMWay firstWay;
    private OSMWay secondWay;

    @BeforeAll
    void prepareWaysFromXmlDocument() {
        // Arrange
        var document = FileLoader.getDocument("OSMTwoWays.xml");
        var mainNode = document.getFirstChild();
        var children = mainNode.getChildNodes();
        var nodes = IterableNodeList.of(children).stream()
                .filter(node -> node.getNodeName().equals("way"))
                .collect(Collectors.toList());
        if (nodes.size() < 2) {
            fail("Invalid xml document provided");
        }

        firstWay = new OSMWay(nodes.get(0));
        secondWay = new OSMWay(nodes.get(1));
    }

    @Test
    void positionBehindFirstNode_pointsOutFirstNodeAsClosest() {
        // Arrange
        var position = Position.of(52.202665, 20.8594);
        String expectedClosestNodeRef = "320278637";

        // Act
        String closestNodeRef = secondWay.findClosestNodeRefTo(position);

        // Assert
        assertEquals(expectedClosestNodeRef, closestNodeRef);
    }

    @Test
    void positionBetweenSecondAndThirdNode_closerToSecondNode_pointsOutSecondNodeAsClosest() {
        // Arrange
        var position = Position.of(52.202766, 20.863103);
        String expectedClosestNodeRef = "320278915";

        // Act
        String closestNodeRef = secondWay.findClosestNodeRefTo(position);

        // Assert
        assertEquals(expectedClosestNodeRef, closestNodeRef);
    }
}
