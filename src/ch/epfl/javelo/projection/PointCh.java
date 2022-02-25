package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

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
        if (e > SwissBounds.MAX_E || e < SwissBounds.MIN_E || n > SwissBounds.MAX_N || n < SwissBounds.MIN_N) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Calculates the distance between this point and point given in argument.
     *
     * @param that the point we calculate distance to
     * @return the distance
     */
    public double distanceTo(PointCh that) {
        double xDistance = this.e - that.e();
        double yDistance = this.n - that.n();

        return Math2.norm(xDistance, yDistance);
    }

    /**
     * Calculates the squared distance between this point and point given in argument.
     *
     * @param that the point we calculate squared distance to
     * @return the squared distance
     */
    public double squaredDistanceTo(PointCh that) {
        double norm = distanceTo(that);
        return norm * norm;
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