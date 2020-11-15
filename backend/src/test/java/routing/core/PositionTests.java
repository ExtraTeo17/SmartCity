package routing.core;

import org.junit.Assert;
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);
        Assert.assertEquals(latB, paris.getLat(), 0);
        Assert.assertEquals(lngB, paris.getLng(), 0);

        var expectedLat = (latA + latB) / 2.0;
        Assert.assertEquals("We are not in Schleiz.", expectedLat, midpoint.getLat(), 0);

        var expectedLng = (lngA + lngB) / 2.0;
        Assert.assertEquals("We are not in Schleiz.", expectedLng, midpoint.getLng(), 0);
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
        Assert.assertEquals(latB, capeTown.getLat(), 0);
        Assert.assertEquals(lngB, capeTown.getLng(), 0);

        var expectedLat = (latA + latB) / 2.0;
        expectedLat = (expectedLat + latB) / 2.0;
        expectedLat = (expectedLat + latB) / 2.0;
        Assert.assertEquals("Warsaw should be in Omaheke now.", expectedLat, warsaw.getLat(), 0);

        var expectedLng = (lngA + lngB) / 2.0;
        expectedLng = (expectedLng + lngB) / 2.0;
        expectedLng = (expectedLng + lngB) / 2.0;
        Assert.assertEquals("Warsaw should be in Omaheke now.", expectedLng, warsaw.getLng(), 0);
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);
        Assert.assertEquals(latB, omaheke.getLat(), 0);
        Assert.assertEquals(lngB, omaheke.getLng(), 0);

        // Assert
        // To acquire better result we would need to include partial computations with different Earth radius
        //  it's about 8_400 km, good enough for that
        double expectedVal = 8_394_866;
        Assert.assertEquals("Distance to Omaheke is different. Did you skip Geography class?\n",
                expectedVal, dist, 1);
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);
        Assert.assertEquals(latB, omaheke.getLat(), 0);
        Assert.assertEquals(lngB, omaheke.getLng(), 0);

        var expectedLat = latA + latB;
        Assert.assertEquals("Sum of coordinates is invalid. Math is not your strong point, isn't it?\n",
                expectedLat, sumVertex.getLat(), 0);

        var expectedLng = lngA + lngB;
        Assert.assertEquals("Sum of coordinates is invalid. Math is not your strong point, isn't it?\n",
                expectedLng, sumVertex.getLng(), 0);
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);
        Assert.assertEquals(latB, omaheke.getLat(), 0);
        Assert.assertEquals(lngB, omaheke.getLng(), 0);

        var expectedLat = latA - latB;
        Assert.assertEquals("Difference of coordinates is invalid. Math is not your strong point, isn't it?\n",
                expectedLat, diffVertex.getLat(), 0);

        var expectedLng = lngA - lngB;
        Assert.assertEquals("Difference of coordinates is invalid. Math is not your strong point, isn't it?\n",
                expectedLng, diffVertex.getLng(), 0);
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);

        var expectedValue = latA * latA + lngA * lngA;
        Assert.assertEquals("Squared sum of coordinates is invalid. Math is not your strong point, isn't it?\n",
                expectedValue, result, 0);
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
        Assert.assertEquals(latA, warsaw.getLat(), 0);
        Assert.assertEquals(lngA, warsaw.getLng(), 0);
        Assert.assertEquals(latB, paris.getLat(), 0);
        Assert.assertEquals(lngB, paris.getLng(), 0);
        Assert.assertEquals(latC, omaheke.getLat(), 0);
        Assert.assertEquals(lngC, omaheke.getLng(), 0);

        // cosC =  (a2 + b2  - c2)/2ab
        var a2 = Math.pow(latA - latB, 2) + Math.pow(lngA - lngB, 2);
        var b2 = Math.pow(latA - latC, 2) + Math.pow(lngA - lngC, 2);
        var c2 = Math.pow(latB - latC, 2) + Math.pow(lngB - lngC, 2);
        var expectedValue = (a2 + b2 - c2) / (2 * Math.sqrt(a2 * b2));
        // To acquire better result we would need to polar angle (angle on sphere),
        //   for 8400km (warsaw-omaheke) is good enough
        Assert.assertEquals("Value of cosine is invalid. You commit sacrilege by not knowing its value.\n",
                expectedValue, saintTriangleCosine, 0.029);
    }
}