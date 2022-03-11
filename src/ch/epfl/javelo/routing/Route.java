package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

public interface Route {

    abstract int indexOfSegmentAt(double position);

    abstract double length();

    abstract List<Edge> edges();

    abstract List<PointCh> points();

    abstract PointCh pointAt(double position);

    abstract int nodeClosestTo(double position);

    abstract RoutePoint pointClosestTo(PointCh point);
}
