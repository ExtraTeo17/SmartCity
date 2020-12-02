package routing.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PositionTests {

    @Test
    void midpoint_once_originalNotModified_correctResult() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        double latB = 48.8566;
        double lngB = 2.3522;
        var paris = Position.of(latB, lngB);

        // Act
        var midpoint = warsaw.midpoint(paris);

        // Assert
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);
        Assertions.assertEquals(latB, paris.getLat(), 0);
        Assertions.assertEquals(lngB, paris.getLng(), 0);

        var expectedLat = (latA + latB) / 2.0;
        Assertions.assertEquals(expectedLat, midpoint.getLat(), 0, "We are not in Schleiz.");

        var expectedLng = (lngA + lngB) / 2.0;
        Assertions.assertEquals(expectedLng, midpoint.getLng(), 0, "We are not in Schleiz.");
    }

    @Test
    void midpoint_recursiveThrice_originalNotModified_correctResult() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        IGeoPosition warsaw = Position.of(latA, lngA);

        double latB = -33.918861;
        double lngB = 18.423300;
        var capeTown = Position.of(latB, lngB);

        // Act
        warsaw = warsaw.midpoint(capeTown);
        warsaw = warsaw.midpoint(capeTown);
        warsaw = warsaw.midpoint(capeTown);

        // Assert
        Assertions.assertEquals(latB, capeTown.getLat(), 0);
        Assertions.assertEquals(lngB, capeTown.getLng(), 0);

        var expectedLat = (latA + latB) / 2.0;
        expectedLat = (expectedLat + latB) / 2.0;
        expectedLat = (expectedLat + latB) / 2.0;
        Assertions.assertEquals(expectedLat, warsaw.getLat(), 0, "Warsaw should be in Omaheke now.");

        var expectedLng = (lngA + lngB) / 2.0;
        expectedLng = (expectedLng + lngB) / 2.0;
        expectedLng = (expectedLng + lngB) / 2.0;
        Assertions.assertEquals(expectedLng, warsaw.getLng(), 0, "Warsaw should be in Omaheke now.");
    }

    @Test
    void distance_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        double latB = -23.149400874999998;
        double lngB = 18.747488750000002;
        var omaheke = Position.of(latB, lngB);

        // Act
        double dist = warsaw.distance(omaheke);
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);
        Assertions.assertEquals(latB, omaheke.getLat(), 0);
        Assertions.assertEquals(lngB, omaheke.getLng(), 0);

        // Assert
        // To acquire better result we would need to include partial computations with different Earth radius
        //  it's about 8_400 km, good enough for that
        double expectedVal = 8_394_866;
        Assertions.assertEquals(
                expectedVal, dist, 1, "Distance to Omaheke is different. Did you skip Geography class?\n");
    }

    @Test
    void sum_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        double latB = -23.149400874999998;
        double lngB = 18.747488750000002;
        var omaheke = Position.of(latB, lngB);

        // Act
        var sumVertex = warsaw.sum(omaheke);

        // Assert
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);
        Assertions.assertEquals(latB, omaheke.getLat(), 0);
        Assertions.assertEquals(lngB, omaheke.getLng(), 0);

        var expectedLat = latA + latB;
        Assertions.assertEquals(
                expectedLat, sumVertex.getLat(), 0, "Sum of coordinates is invalid. Math is not your strong point, isn't it?\n");

        var expectedLng = lngA + lngB;
        Assertions.assertEquals(
                expectedLng, sumVertex.getLng(), 0, "Sum of coordinates is invalid. Math is not your strong point, isn't it?\n");
    }

    @Test
    void diff_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        double latB = -23.149400874999998;
        double lngB = 18.747488750000002;
        var omaheke = Position.of(latB, lngB);

        // Act
        var diffVertex = warsaw.diff(omaheke);

        // Assert
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);
        Assertions.assertEquals(latB, omaheke.getLat(), 0);
        Assertions.assertEquals(lngB, omaheke.getLng(), 0);

        var expectedLat = latA - latB;
        Assertions.assertEquals(
                expectedLat, diffVertex.getLat(), 0, "Difference of coordinates is invalid. Math is not your strong point, isn't it?\n");

        var expectedLng = lngA - lngB;
        Assertions.assertEquals(
                expectedLng, diffVertex.getLng(), 0, "Difference of coordinates is invalid. Math is not your strong point, isn't it?\n");
    }

    @Test
    void squaredSum_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        // Act
        var result = warsaw.squaredSum();

        // Assert
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);

        var expectedValue = latA * latA + lngA * lngA;
        Assertions.assertEquals(
                expectedValue, result, 0, "Squared sum of coordinates is invalid. Math is not your strong point, isn't it?\n");
    }

    @Test
    void cosineAngle_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);

        double latB = 48.8566;
        double lngB = 2.3522;
        var paris = Position.of(latB, lngB);

        double latC = -23.149400874999998;
        double lngC = 18.747488750000002;
        var omaheke = Position.of(latC, lngC);

        // Act
        double saintTriangleCosine = warsaw.cosineAngle(paris, omaheke);

        // Assert
        Assertions.assertEquals(latA, warsaw.getLat(), 0);
        Assertions.assertEquals(lngA, warsaw.getLng(), 0);
        Assertions.assertEquals(latB, paris.getLat(), 0);
        Assertions.assertEquals(lngB, paris.getLng(), 0);
        Assertions.assertEquals(latC, omaheke.getLat(), 0);
        Assertions.assertEquals(lngC, omaheke.getLng(), 0);

        // cosC =  (a2 + b2  - c2)/2ab
        var a2 = Math.pow(latA - latB, 2) + Math.pow(lngA - lngB, 2);
        var b2 = Math.pow(latA - latC, 2) + Math.pow(lngA - lngC, 2);
        var c2 = Math.pow(latB - latC, 2) + Math.pow(lngB - lngC, 2);
        var expectedValue = (a2 + b2 - c2) / (2 * Math.sqrt(a2 * b2));
        // To acquire better result we would need to polar angle (angle on sphere),
        //   for 8400km (warsaw-omaheke) is good enough
        Assertions.assertEquals(
                expectedValue, saintTriangleCosine, 0.029, "Value of cosine is invalid. You commit sacrilege by not knowing its value.\n");
    }
}