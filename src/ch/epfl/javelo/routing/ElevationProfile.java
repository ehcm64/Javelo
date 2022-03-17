package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;

/**
 * Represents the elevation profile of an itinerary.
 *
 * @author Edouard Mignan (345875)
 */
public class ElevationProfile {
    private final double length;
    private final float[] elevationSamples;

    /**
     * Creates an elevation profile.
     *
     * @param length           the length of the profile
     * @param elevationSamples the array containing the elevations (float) at fixed intervals
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = new float[elevationSamples.length];
        for (int i = 0; i < elevationSamples.length; i++) {
            this.elevationSamples[i] = elevationSamples[i];
        }
    }

    /**
     * Returns the length of the elevation profile.
     *
     * @return the length of the elevation profile
     */
    public double length() {
        return this.length;
    }

    /**
     * Returns the smallest altitude in the elevation profile.
     *
     * @return the smallest altitude in the array of elevations
     */
    public double minElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevation : this.elevationSamples) {
            s.accept(elevation);
        }
        return s.getMin();
    }

    /**
     * Returns the greatest altitude in the elevation profile.
     *
     * @return the greatest altitude in the array of elevations
     */
    public double maxElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevation : this.elevationSamples) {
            s.accept(elevation);
        }
        return s.getMax();
    }

    /**
     * Returns the total ascent of the elevation profile
     * (sum of positive deltas between each altitude couple of the elevation profile).
     *
     * @return the total ascent of the elevation profile
     */
    public double totalAscent() {
        double totalAscent = 0;
        for (int i = 1; i < this.elevationSamples.length; i++) {
            double firstElevation = this.elevationSamples[i - 1];
            double secondElevation = this.elevationSamples[i];
            double delta = secondElevation - firstElevation;
            if (delta > 0) totalAscent += delta;
        }
        return totalAscent;
    }

    /**
     * Returns the total descent of the elevation profile
     * (sum of negative deltas between each altitude couple of the elevation profile).
     *
     * @return the total descent of the elevation profile
     */
    public double totalDescent() {
        double totalDescent = 0;
        for (int i = 1; i < this.elevationSamples.length; i++) {
            double firstElevation = this.elevationSamples[i - 1];
            double secondElevation = this.elevationSamples[i];
            double delta = secondElevation - firstElevation;
            if (delta < 0) totalDescent -= delta;
        }
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
        return Functions.sampled(this.elevationSamples, this.length).applyAsDouble(position);
    }
}