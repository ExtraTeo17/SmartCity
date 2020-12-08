package routing.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import smartcity.config.ConfigMutator;
import testutils.ReflectionHelper;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ZoneTests {

    static Stream<Arguments> parisZoneProvider() {
        return Stream.of(
                arguments(4250, true),
                arguments(4150, false)
        );
    }

    @ParameterizedTest
    @MethodSource("parisZoneProvider")
    void isInZone_parisWithin4500m_correctResult(int radius, boolean expectedResult) {
        // Arrange
        double latA = 48.8566;
        double lngA = 2.3522;
        var parisHotelDeVille = Position.of(latA, lngA);
        var zone = new Zone(parisHotelDeVille, radius);

        double latB = 48.8584;
        double lngB = 2.2945;
        var eiffelTower = Position.of(latB, lngB);

        // Act
        boolean result = zone.contains(eiffelTower);

        // Assert
        Assertions.assertEquals(latA, parisHotelDeVille.getLat(), 0);
        Assertions.assertEquals(lngA, parisHotelDeVille.getLng(), 0);
        Assertions.assertEquals(parisHotelDeVille.compareTo(zone.getCenter()), 0);
        Assertions.assertEquals(radius, zone.getRadius());
        Assertions.assertEquals(latB, eiffelTower.getLat(), 0);
        Assertions.assertEquals(lngB, eiffelTower.getLng(), 0);

        Assertions.assertEquals(expectedResult, result, "Eiffel Tower is in Paris, isn't it?");
    }

    static Stream<Arguments> warsawZoneProvider() {
        return Stream.of(
                arguments(300, true),
                arguments(290, false)
        );
    }

    @ParameterizedTest
    @MethodSource("warsawZoneProvider")
    void isInZone_warsawWithin300m_correctResult(int radius, boolean expectedResult) {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsawTD = Position.of(latA, lngA);
        var zone = new Zone(warsawTD, radius);

        double latB = 52.239390;
        double lngB = 21.015518;
        var monumentToBartolomeoColleoni = Position.of(latB, lngB);

        // Act
        boolean result = zone.contains(monumentToBartolomeoColleoni);

        // Assert
        Assertions.assertEquals(latA, warsawTD.getLat(), 0);
        Assertions.assertEquals(lngA, warsawTD.getLng(), 0);
        Assertions.assertEquals(warsawTD.compareTo(zone.getCenter()), 0);
        Assertions.assertEquals(radius, zone.getRadius());
        Assertions.assertEquals(latB, monumentToBartolomeoColleoni.getLat(), 0);
        Assertions.assertEquals(lngB, monumentToBartolomeoColleoni.getLng(), 0);

        Assertions.assertEquals(expectedResult, result, "Bartolomeo Colleoni is disappointed with you");
    }

    @Test
    void setZone_happyPath() {
        // Arrange
        double latA = 52.23682;
        double lngA = 21.01681;
        var warsaw = Position.of(latA, lngA);
        int radius = 1000;
        var warsawZone = new Zone(warsaw, radius);
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        var mutator = new ConfigMutator() {
            private void setZone(IGeoPosition pos, int radius) {
                warsawZone.set(mutation, pos, radius);
            }
        };

        double latB = 48.8566;
        double lngB = 2.3522;
        IGeoPosition newLocation = Position.of(latB, lngB);
        int newRadius = 2000;

        // Act
        mutator.setZone(newLocation, newRadius);

        // Assert
        Assertions.assertEquals(newLocation.compareTo(warsawZone.getCenter()), 0);
        Assertions.assertEquals(newRadius, warsawZone.getRadius());
    }

    @Test
    void setZone_secondMutator_throwsException() {
        // Arrange
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        new ConfigMutator() {};

        // Act & Assert
        assertThrows(IllegalCallerException.class,
                () -> new ConfigMutator() {},
                "You should not try to create second mutator.\n");
    }
}