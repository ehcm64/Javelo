package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
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
        double positionMinusSegments = Math2.clamp(0, position, this.length());
        int index = 0;
        for (Route segment : this.segments) {
            if (positionMinusSegments <= segment.length()) {
                index += segment.indexOfSegmentAt(positionMinusSegments);
            }
            positionMinusSegments -= segment.length();
            index += segment.indexOfSegmentAt(segment.length()) + 1;
        }
        return index;
    }

    @Override
    public double length() {
        double totalLength = 0;
        for (Route segment : this.segments) {
            totalLength += segment.length();
        }
        return totalLength;
    }

    @Override
    public List<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (Route segment : this.segments) {
            edges.addAll(segment.edges());
        }
        return edges;
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        for (Route segment : this.segments) {
            points.addAll(segment.points());
        }
        return points;
    }

    @Override
    public PointCh pointAt(double position) {
        double positionMinusSegments = Math2.clamp(0, position, this.length());
        for (Route segment : this.segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.pointAt(positionMinusSegments);
            }
            positionMinusSegments -= segment.length();
        }
        return pointAt(this.length());
    }

    @Override
    public double elevationAt(double position) {
        double positionMinusSegments = Math2.clamp(0, position, this.length());
        for (Route segment : this.segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.elevationAt(positionMinusSegments);
            }
            positionMinusSegments -= segment.length();
        }
        return elevationAt(this.length());
    }

    @Override
    public int nodeClosestTo(double position) {
        double positionMinusSegments = Math2.clamp(0, position, this.length());
        for (Route segment : this.segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.nodeClosestTo(positionMinusSegments);
            }
            positionMinusSegments -= segment.length();
        }
        return nodeClosestTo(this.length());
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double minDistance = Double.POSITIVE_INFINITY;
        RoutePoint closestPoint = null;
        for (Route segment : this.segments) {
            RoutePoint testPoint = segment.pointClosestTo(point);
            if (testPoint.distanceToReference() < minDistance) {
                minDistance = testPoint.distanceToReference();
                closestPoint = testPoint;
            }
        }
        return closestPoint;
    }
}
