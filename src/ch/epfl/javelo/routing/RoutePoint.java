package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

public record RoutePoint(PointCh point, double position, double distanceToReference) {
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return null;
    }

    public RoutePoint min(RoutePoint that) {
        if (this.distanceToReference <= that.distanceToReference) {
            return this;
        }
        return that;
    }

    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {

        if (this.distanceToReference <= thatDistanceToReference) {
            return this;
        }
        return new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}
