package osmproxy.elements;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import testutils.FileLoader;
import utilities.IterableNodeList;

import java.util.stream.Collectors;

class OSMWay_determineRouteOrientationAndFilterRelevantNodes_Tests {
    // TODO: More cases with tangent nodes
    @Test
    void startFromFirst_setsFrontOrientation() {
        // Arrange
        var document = FileLoader.getDocument("OSMTwoWays.xml");
        var mainNode = document.getFirstChild();
        var children = mainNode.getChildNodes();
        var nodes = IterableNodeList.of(children).stream()
                .filter(node -> node.getNodeName().equals("way"))
                .collect(Collectors.toList());
        if (nodes.size() < 2) {
            Assert.fail("Invalid xml document provided");
        }

        var firstWay = new OSMWay(nodes.get(0));
        var secondWay = new OSMWay(nodes.get(1));

        // Act
        int result = firstWay.determineRouteOrientationAndFilterRelevantNodes(secondWay, 0);

        // Assert
        Assert.assertEquals(3, result);
        Assert.assertEquals(OSMWay.RouteOrientation.FRONT, firstWay.getRouteOrientation());
    }

    @Test
    void startFromLast_setsBackOrientation() {
        // Arrange
        var document = FileLoader.getDocument("OSMTwoWays.xml");
        var mainNode = document.getFirstChild();
        var children = mainNode.getChildNodes();
        var nodes = IterableNodeList.of(children).stream()
                .filter(node -> node.getNodeName().equals("way"))
                .collect(Collectors.toList());
        if (nodes.size() < 2) {
            Assert.fail("Invalid xml document provided");
        }

        var firstWay = new OSMWay(nodes.get(0));
        var secondWay = new OSMWay(nodes.get(1));

        // Act
        int result = firstWay.determineRouteOrientationAndFilterRelevantNodes(secondWay, 3);

        // Assert
        Assert.assertEquals(3, result);
        Assert.assertEquals(OSMWay.RouteOrientation.BACK, firstWay.getRouteOrientation());
    }
}