package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

public record RoutePoint(PointCh point, double position, double distanceToReference) {
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return null;
    }

    public RoutePoint min(RoutePoint that) {
        return null;
    }

    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return null;
    }
}
