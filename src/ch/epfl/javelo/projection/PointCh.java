package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * A point in the swiss coordinate system.
 *
 * @author Timo Moebel (345665)
 */
public record PointCh(double e, double n) {

    /**
     * Creates a point with given east and north coordinates.
     *
     * @param e the east coordinate of the point
     * @param n the north coordinate of the point
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Calculates the startDistance between this point and point given in argument.
     *
     * @param that the point we calculate startDistance to
     * @return the startDistance
     */
    public double distanceTo(PointCh that) {
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Calculates the squared startDistance between this point and point given in argument.
     *
     * @param that the point we calculate squared startDistance to
     * @return the squared startDistance
     */
    public double squaredDistanceTo(PointCh that) {
        return Math2.squaredNorm(e - that.e, n - that.n);
    }

    /**
     * Calculates the longitude of this point.
     *
     * @return the longitude of this point
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }

    /**
     * Calculates the latitude of this point.
     *
     * @return the latitude of this point
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }
}