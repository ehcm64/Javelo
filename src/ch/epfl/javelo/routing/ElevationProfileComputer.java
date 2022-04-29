package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;

/**
 * Represents an elevation profile calculator.
 *
 * @author Edouard Mignan (345875) and Timo Moebel (345665)
 */
public final class ElevationProfileComputer {

    private ElevationProfileComputer() {
    }

    /**
     * Returns the elevation profile of a given route.
     *
     * @param route         the route
     * @param maxStepLength the maximum length of the step in the elevation profile
     * @return the elevation profile
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);

        int nbOfSamples = (int) Math.ceil(route.length() / maxStepLength) + 1;
        double stepLength = route.length() / (nbOfSamples - 1);
        float[] elevationSamples = new float[nbOfSamples];

        // get all elevations (even if they are NaN) from all edges at fixed step length
        for (int i = 0; i < nbOfSamples; i++) {
            double position = i * stepLength;
            elevationSamples[i] = (float) route.elevationAt(position);
        }

        int lastIndex = elevationSamples.length - 1;
        float firstSample = elevationSamples[0];
        float lastSample = elevationSamples[lastIndex];
        int firstRealIndex = 0;
        int lastRealIndex = 0;

        // replace NaN in head and tail of array by closest real elevations
        if (Float.isNaN(firstSample) && containsRealValue(elevationSamples)) {
            firstRealIndex = nextRealIndex(elevationSamples, 0);
            Arrays.fill(elevationSamples,
                    0,
                    firstRealIndex,
                    elevationSamples[firstRealIndex]);
        }
        if (Float.isNaN(lastSample) && containsRealValue(elevationSamples)) {
            lastRealIndex = previousRealIndex(elevationSamples, lastIndex);
            Arrays.fill(elevationSamples,
                    lastRealIndex + 1,
                    lastIndex + 1,
                    elevationSamples[lastRealIndex]);
        }
        if (!containsRealValue(elevationSamples)) {
            Arrays.fill(elevationSamples, 0, lastIndex + 1, 0);
            return new ElevationProfile(route.length(), elevationSamples);
        }

        // replace NaN holes in array by interpolation from the closest real values
        int NaNIndex = firstNaNIndex(elevationSamples, firstRealIndex, lastRealIndex);
        while (NaNIndex != 0) {
            int nextRealValueIndex = nextRealIndex(elevationSamples, NaNIndex);
            double distance = (nextRealValueIndex - NaNIndex + 1);
            elevationSamples[NaNIndex] = (float) Math2.interpolate(
                    elevationSamples[NaNIndex - 1],
                    elevationSamples[nextRealValueIndex],
                    1 / distance);
            NaNIndex = firstNaNIndex(elevationSamples, NaNIndex, lastRealIndex);
        }
        return new ElevationProfile(route.length(), elevationSamples);
    }

    private static int nextRealIndex(float[] samples, int start) {
        int i = start;
        while (Float.isNaN(samples[i])) {
            i++;
        }
        return i;
    }

    private static int previousRealIndex(float[] samples, int start) {
        int i = start;
        while (Float.isNaN(samples[i])) {
            i--;
        }
        return i;
    }

    private static boolean containsRealValue(float[] samples) {
        for (float sample : samples) {
            if (!Float.isNaN(sample)) return true;
        }
        return false;
    }

    private static int firstNaNIndex(float[] samples, int startIndex, int endIndex) {
        Preconditions.checkArgument(startIndex < samples.length
                && endIndex < samples.length);
        for (int i = startIndex; i < samples.length; i++) {
            if (Float.isNaN(samples[i])) return i;
        }
        return 0;
    }
}
