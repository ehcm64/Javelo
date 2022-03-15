package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

public record RoutePoint(PointCh point, double position, double distanceToReference) {
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(this.point, this.position + positionDifference, this.distanceToReference);
    }

    public RoutePoint min(RoutePoint that) {
        return this.distanceToReference <= that.distanceToReference ? this : that;
    }

    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return this.distanceToReference <= thatDistanceToReference ? this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}
