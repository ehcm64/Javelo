package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * Represents the closest point of an itinerary to a reference point.
 *
 * @author Timo Moebel (345665)
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {
    public static final RoutePoint NONE =
            new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    /**
     * Returns an identical point but with position shifted by a given difference.
     *
     * @param positionDifference the position difference
     * @return the same point but with a different position attribute
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(this.point,
                this.position + positionDifference,
                this.distanceToReference);
    }

    /**
     * Returns closest point to reference point between this point or another given point.
     *
     * @param that the other point
     * @return the closest point to the reference point
     */
    public RoutePoint min(RoutePoint that) {
        return this.distanceToReference <= that.distanceToReference ? this : that;
    }

    /**
     * Returns this point if it is closer to reference than other given point,
     * else returns new point with attributes of given point.
     *
     * @param thatPoint               other point
     * @param thatPosition            other point's position
     * @param thatDistanceToReference other point's startDistance to reference point
     * @return the closest RoutePoint
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return this.distanceToReference <= thatDistanceToReference ?
                this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}
