package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents the elevation profile of an itinerary.
 *
 * @author Edouard Mignan (345875)
 */
public final class ElevationProfile {
    private final double length;
    private final float[] elevationSamples;

    private final double minElevation;
    private final double maxElevation;
    private final double totalAscent;
    private final double totalDescent;

    /**
     * Creates an elevation profile.
     *
     * @param length           the length of the profile
     * @param elevationSamples the array containing the elevations (float) at fixed intervals
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0
                && elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = new float[elevationSamples.length];
        System.arraycopy(elevationSamples, 0,
                this.elevationSamples, 0,
                elevationSamples.length);
        DoubleSummaryStatistics samplesStatistics = new DoubleSummaryStatistics();
        for (float elevation : this.elevationSamples) {
            samplesStatistics.accept(elevation);
        }

        minElevation = samplesStatistics.getMin();
        maxElevation = samplesStatistics.getMax();
        totalDescent = computeTotalDescent();
        totalAscent = computeTotalAscent();
    }

    /**
     * Returns the length of the elevation profile.
     *
     * @return the length of the elevation profile
     */
    public double length() {
        return length;
    }

    /**
     * Returns the smallest altitude in the elevation profile.
     *
     * @return the smallest altitude in the array of elevations
     */
    public double minElevation() {
        return minElevation;
    }

    /**
     * Returns the greatest altitude in the elevation profile.
     *
     * @return the greatest altitude in the array of elevations
     */
    public double maxElevation() {
        return maxElevation;
    }

    /**
     * Returns the total ascent of the elevation profile
     * (sum of positive deltas between each altitude couple of the elevation profile).
     *
     * @return the total ascent of the elevation profile
     */
    public double totalAscent() {
        return totalAscent;
    }

    /**
     * Returns the total descent of the elevation profile
     * (sum of negative deltas between each altitude couple of the elevation profile).
     *
     * @return the total descent of the elevation profile
     */
    public double totalDescent() {
        return totalDescent;
    }

    /**
     * Returns the elevation at a given position in the elevation profile.
     *
     * @param position the position
     *                 (should be between 0 and the length of the profile but can be negative or greater than the length)
     * @return the elevation at the given position
     */
    public double elevationAt(double position) {
        DoubleUnaryOperator function = Functions.sampled(
                elevationSamples,
                length);
        return function.applyAsDouble(position);
    }

    private double computeTotalAscent() {
        double totalAscent = 0;
        for (int i = 1; i < elevationSamples.length; i++) {
            double firstElevation = elevationSamples[i - 1];
            double secondElevation = elevationSamples[i];
            double delta = secondElevation - firstElevation;
            if (delta > 0) totalAscent += delta;
        }
        return totalAscent;
    }

    private double computeTotalDescent() {
        double totalDescent = 0;
        for (int i = 1; i < elevationSamples.length; i++) {
            double firstElevation = elevationSamples[i - 1];
            double secondElevation = elevationSamples[i];
            double delta = secondElevation - firstElevation;
            if (delta < 0) totalDescent -= delta;
        }
        return totalDescent;
    }
}