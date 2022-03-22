package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;

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
        int nbOfSamples = (int) Math.ceil(route.length() / maxStepLength) + 1;
        float stepLength = (float) ((route.length() / (nbOfSamples - 1)));
        float[] elevationSamples = new float[nbOfSamples];
        double alongEdgePosition = 0;
        int samplesIndex = 0;

        for (int edgeIndex = 0; edgeIndex < route.edges().size(); edgeIndex++) {
            Edge edge = route.edges().get(edgeIndex);
            if (edgeIndex != 0) alongEdgePosition -= route.edges().get(edgeIndex - 1).length();
            while (alongEdgePosition <= edge.length()) {
                elevationSamples[samplesIndex] = (float) edge.elevationAt(alongEdgePosition);
                samplesIndex++;
                alongEdgePosition += stepLength;
            }
        }

        if (arrayContainsNaN(elevationSamples)) {
            if (arrayContainsRealValue(elevationSamples)) {
                int realValueIndex = closestUpperRealElevationIndex(elevationSamples, 0);
                Arrays.fill(elevationSamples, 0, realValueIndex - 1, elevationSamples[realValueIndex]);
                realValueIndex = closestLowerRealElevationIndex(elevationSamples, elevationSamples.length - 1);
                Arrays.fill(elevationSamples, realValueIndex + 1, elevationSamples.length - 1, elevationSamples[realValueIndex]);
            } else {
                Arrays.fill(elevationSamples, 0, elevationSamples.length - 1, 0);
            }

            while (arrayContainsNaN(elevationSamples)) {
                int firstNaNIndex = findFirstNaNIndex(elevationSamples);
                int nextRealValueIndex = closestUpperRealElevationIndex(elevationSamples, firstNaNIndex);
                double distance = (nextRealValueIndex - firstNaNIndex + 1) * stepLength;

                elevationSamples[firstNaNIndex] = (float) Math2.interpolate(elevationSamples[firstNaNIndex - 1],
                        elevationSamples[nextRealValueIndex],
                        stepLength / distance);
            }
        }
        return new ElevationProfile(route.length(), elevationSamples);
    }

    private static int closestUpperRealElevationIndex(float[] samples, int index) {
        int i = index;
        while (Float.isNaN(samples[i])) {
            i++;
        }
        return i;
    }

    private static int closestLowerRealElevationIndex(float[] samples, int index) {
        int i = index;
        while (Float.isNaN(samples[i])) {
            i--;
        }
        return i;
    }

    private static boolean arrayContainsRealValue(float[] samples) {
        for (float sample : samples) {
            if (!Float.isNaN(sample)) return true;
        }
        return false;
    }

    private static boolean arrayContainsNaN(float[] samples) {
        for (float sample : samples) {
            if (Float.isNaN(sample)) return true;
        }
        return false;
    }

    private static int findFirstNaNIndex(float[] samples) {
        for (int i = 0; i < samples.length; i++) {
            if (Float.isNaN(samples[i])) return i;
        }
        return 0;
    }
}
