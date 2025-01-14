package agents;

import mocks.ContainerControllerMock;
import org.junit.jupiter.api.Test;
import routing.RoutingConstants;
import routing.core.IGeoPosition;
import smartcity.config.StaticConfig;
import smartcity.lights.core.Light;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static agents.AgentUtils.setupAgentsCreator;
import static org.junit.jupiter.api.Assertions.*;

class AgentsPreparerTests {


    @Test
    void tryConstructLightManagers_defaultCarZone() {
        if (StaticConfig.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
            fail("Test is designed for new solution");
        }

        // Arrange
        var agentsContainer = new HashAgentsContainer(new ContainerControllerMock());
        agentsContainer.register(LightManagerAgent.class);
        var creator = setupAgentsCreator(agentsContainer);

        // Act
        var success = creator.tryConstructLightManagers();

        // Assert
        assertTrue(success);
        assertEquals(5, agentsContainer.size(LightManagerAgent.class));

        var context = new Object() {
            private final List<IGeoPosition> allPositions = new ArrayList<>();
        };
        agentsContainer.forEach(LightManagerAgent.class, manager -> {
            var lights = manager.getLights();
            // Assumptions for current car zone - change if algorithm changes
            assertTrue(lights.size() > 2);
            var lightsPartition = lights.stream().collect(Collectors.partitioningBy(Light::isGreen));
            var greenLights = lightsPartition.get(true);
            var redLights = lightsPartition.get(false);
            assertAll("At least one light should be green and at least one light should be red!",
                    () -> assertTrue(greenLights.size() > 0),
                    () -> assertTrue(redLights.size() > 0));
            assertLightsCorrectForOsmId(String.valueOf(lights.get(0).getOsmLightId()), greenLights, redLights);

            context.allPositions.addAll(lights);
        });

        var allPositions = context.allPositions;
        assertEquals(16, allPositions.size());
        for (int i = 0; i < 16; ++i) {
            var posA = allPositions.get(i);
            for (int j = i + 1; j < 16; ++j) {
                var posB = allPositions.get(j);
                assertTrue(posA.compareTo(posB) != 0, "No 2 positions of lights should be equal");
                var distance = posA.distance(posB) * RoutingConstants.METERS_PER_DEGREE;
                assertTrue(distance > 5, "Distance between each light should be at least 5 meters\n" +
                        "  distance:" + distance);
            }
        }

    }

    private void assertLightsCorrectForOsmId(String osmId, List<Light> greenLights, List<Light> redLights) {

        switch (osmId) {
            case "32528268" -> assertCorrectGrouping(greenLights, redLights, 2, 2, 709895408L, 48279420);
            case "32892162" -> assertCorrectGrouping(greenLights, redLights, 1, 2, 218386444L, 218386450L);
            case "3378411269" -> assertCorrectGrouping(greenLights, redLights, 2, 1, 331647914, 335238477);
            case "224829889" -> assertCorrectGrouping(greenLights, redLights, 2, 1, 236238463, 316067494);
            case "1321790320" -> assertCorrectGrouping(greenLights, redLights, 1, 2, 117656398, 426582806);

            default -> fail("Unrecognized osmLightId" + osmId);
        }
    }

    private void assertCorrectGrouping(List<Light> greenLights, List<Light> redLights, int sizeA, int sizeB,
                                       long... adjacentIds) {
        if (greenLights.size() == sizeA) {
            assertEquals(sizeB, redLights.size(), String.format("Expected group sizes: (%d, %d)", sizeA, sizeB));
        }
        else {
            assertEquals(sizeA, redLights.size(), String.format("Expected group sizes: (%d, %d)", sizeA, sizeB));
        }

        Predicate<Light> areSameGroup = l -> l.getAdjacentWayId() == adjacentIds[0]
                || l.getAdjacentWayId() == adjacentIds[1];
        assertTrue(greenLights.stream().allMatch(areSameGroup) ||
                        redLights.stream().allMatch(areSameGroup),
                "This lights should be in same group: (" + adjacentIds[0] + "," + adjacentIds[1] + ")");
    }


}