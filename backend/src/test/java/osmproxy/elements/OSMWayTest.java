package osmproxy.elements;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import utilities.IterableNodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

class OSMWayTest {
    private static DocumentBuilder builder;
    private static final String resourcePath = "src/test/resources/";
    private Document document;

    @BeforeAll
    static void setupAll() {
        var factory = DocumentBuilderFactory.newDefaultInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void setupDocument(String filename) {
        File file = new File(resourcePath + filename);
        try {
            document = builder.parse(file);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }


    // TODO: More cases with tangent nodes
    @Test
    void determineRouteOrientationAndFilterRelevantNodes_startFromFirst_setsFrontOrientation() {
        // Arrange
        setupDocument("OSMTwoWays.xml");
        var mainNode = document.getFirstChild();
        var children = mainNode.getChildNodes();
        var nodes = IterableNodeList.create(children).stream()
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
    void determineRouteOrientationAndFilterRelevantNodes_startFromLast_setsBackOrientation() {
        // Arrange
        setupDocument("OSMTwoWays.xml");
        var mainNode = document.getFirstChild();
        var children = mainNode.getChildNodes();
        var nodes = IterableNodeList.create(children).stream()
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