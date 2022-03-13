package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;

public class ElevationProfile {
    private final double length;
    private final float[] elevationSamples;

    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = new float[elevationSamples.length];
        for (int i = 0; i < elevationSamples.length; i++) {
            this.elevationSamples[i] = elevationSamples[i];
        }
    }

    public double length() {
        return this.length;
    }

    public double minElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevation : this.elevationSamples) {
            s.accept(elevation);
        }
        return s.getMin();
    }

    public double maxElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevation : this.elevationSamples) {
            s.accept(elevation);
        }
        return s.getMax();
    }

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

    public double totalDescent() {
        double totalDescent = 0;
        for (int i = 1; i < this.elevationSamples.length; i++) {
            double firstElevation = this.elevationSamples[i - 1];
            double secondElevation = this.elevationSamples[i];
            double delta = secondElevation - firstElevation;
            if (delta < 0) totalDescent += delta;
        }
        return totalDescent;
    }

    public double elevationAt(double position) {
        return Functions.sampled(this.elevationSamples, this.length).applyAsDouble(position);
    }
}