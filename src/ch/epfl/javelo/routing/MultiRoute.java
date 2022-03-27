package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Represents an itinerary containing multiple segments.
 *
 * @author Timo Moebel (345665) and Edouard Mignan (345875)
 */
public final class MultiRoute implements Route {
    private final List<Route> segments;

    /**
     * Creates a multi-segment itinerary from a given list of segments.
     *
     * @param segments the list of segments
     */
    public MultiRoute(List<Route> segments) {
        this.segments = List.copyOf(segments);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        return 0;
    }

    @Override
    public List<Edge> edges() {
        return null;
    }

    @Override
    public List<PointCh> points() {
        return null;
    }

    @Override
    public PointCh pointAt(double position) {
        return null;
    }

    @Override
    public double elevationAt(double position) {
        return 0;
    }

    @Override
    public int nodeClosestTo(double position) {
        return 0;
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        return null;
    }
}
