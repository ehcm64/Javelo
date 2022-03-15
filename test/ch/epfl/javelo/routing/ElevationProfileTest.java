package ch.epfl.javelo.routing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElevationProfileTest {

    @Test
    void minElevation() {
        float[] elevationSamples = {10, 12, 5, 6, 9};
        ElevationProfile profile = new ElevationProfile(10, elevationSamples);
        assertEquals(5, profile.minElevation());
    }

    @Test
    void maxElevation() {
        float[] elevationSamples = {10, 12, 5, 6, 9};
        ElevationProfile profile = new ElevationProfile(10, elevationSamples);
        assertEquals(12, profile.maxElevation());
    }

    @Test
    void totalAscent() {
        float[] elevationSamples = {10, 12, 5, 6, 9};
        ElevationProfile profile = new ElevationProfile(10, elevationSamples);
        assertEquals(6, profile.totalAscent());
    }

    @Test
    void totalDescent() {
        float[] elevationSamples = {10, 12, 5, 6, 9};
        ElevationProfile profile = new ElevationProfile(10, elevationSamples);
        assertEquals(7, profile.totalDescent());
    }

    @Test
    void elevationAt() {
        float[] elevationSamples = {10, 12, 5, 6, 9, 8};
        ElevationProfile profile = new ElevationProfile(5, elevationSamples);
        assertEquals(11, profile.elevationAt(0.5));
    }
}