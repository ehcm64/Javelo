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
        float stepLength = (float) (route.length() / (nbOfSamples - 1));
        float[] elevationSamples = new float[nbOfSamples];
        double alongEdgePosition = 0;
        int samplesIndex = 0;

        // get all elevations (even if they are NaN) from all edges at fixed step length
        for (int edgeIndex = 0; edgeIndex < route.edges().size(); edgeIndex++) {
            Edge edge = route.edges().get(edgeIndex);
            if (edgeIndex != 0)
                alongEdgePosition -= route.edges().get(edgeIndex - 1).length();
            while (alongEdgePosition <= edge.length()) {
                elevationSamples[samplesIndex] = (float) edge.elevationAt(alongEdgePosition);
                samplesIndex++;
                alongEdgePosition += stepLength;
            }
        }

        int LastIndex = elevationSamples.length - 1;

        // replace NaN in head and tail of array by closest real elevations
        if (Float.isNaN(elevationSamples[0]) && containsRealValue(elevationSamples)) {
            int nextRealIndex = nextRealIndex(elevationSamples, 0);
            Arrays.fill(elevationSamples,
                    0,
                    nextRealIndex,
                    elevationSamples[nextRealIndex]);
        }
        if (Float.isNaN(elevationSamples[LastIndex]) && containsRealValue(elevationSamples)) {
            int previousRealIndex = previousRealIndex(elevationSamples, LastIndex);
            Arrays.fill(elevationSamples,
                    previousRealIndex + 1,
                    LastIndex + 1,
                    elevationSamples[previousRealIndex]);
        }
        if (!containsRealValue(elevationSamples)) {
            Arrays.fill(elevationSamples, 0, LastIndex + 1, 0);
        }

        // replace NaN holes in array by interpolation from the closest real values
        while (containsNaN(elevationSamples)) {
            int NaNIndex = firstNaNIndex(elevationSamples);
            int nextRealValueIndex = nextRealIndex(elevationSamples, NaNIndex);
            double distance = (nextRealValueIndex - NaNIndex + 1);
            elevationSamples[NaNIndex] = (float) Math2.interpolate(
                    elevationSamples[NaNIndex - 1],
                    elevationSamples[nextRealValueIndex],
                    1 / distance);
        }
        return new ElevationProfile(route.length(), elevationSamples);
    }

    private static int nextRealIndex(float[] samples, int index) {
        int i = index;
        while (Float.isNaN(samples[i])) {
            i++;
        }
        return i;
    }

    private static int previousRealIndex(float[] samples, int index) {
        int i = index;
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

    private static boolean containsNaN(float[] samples) {
        for (float sample : samples) {
            if (Float.isNaN(sample)) return true;
        }
        return false;
    }

    private static int firstNaNIndex(float[] samples) {
        for (int i = 0; i < samples.length; i++) {
            if (Float.isNaN(samples[i])) return i;
        }
        return 0;
    }
}
