package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
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
        Preconditions.checkArgument(segments.size() != 0);
        this.segments = List.copyOf(segments);
    }

    /**
     * Returns the index of the segment at the given position.
     *
     * @param position the position
     * @return the index of the segment
     */
    @Override
    public int indexOfSegmentAt(double position) {
        double positionMinusSegments = Math2.clamp(0, position, length());
        int index = 0;
        for (Route segment : segments) {
            if (positionMinusSegments <= segment.length()) {
                index += segment.indexOfSegmentAt(positionMinusSegments);
                break;
            } else {
                positionMinusSegments -= segment.length();
                index += segment.indexOfSegmentAt(segment.length()) + 1;
            }
        }
        return index;
    }

    /**
     * Returns the length of the itinerary in meters.
     *
     * @return the length of the itinerary in meters
     */
    @Override
    public double length() {
        double totalLength = 0;
        for (Route segment : segments) {
            totalLength += segment.length();
        }
        return totalLength;
    }

    /**
     * Returns the entirety of edges in the itinerary.
     *
     * @return the list of edges
     */
    @Override
    public List<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (Route segment : segments) {
            edges.addAll(segment.edges());
        }
        return edges;
    }

    /**
     * Returns all the points located at the extremities of the edges
     * of the itinerary.
     *
     * @return the list of points
     */
    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        points.add(pointAt(0));
        for (Route segment : segments) {
            List<PointCh> pointsMinus0 = new ArrayList<>(segment.points());
            pointsMinus0.remove(0);
            points.addAll(pointsMinus0);
        }
        return points;
    }

    /**
     * Returns the point in swiss coordinates at the given position in the itinerary.
     *
     * @param position the position of the point
     * @return the point in swiss coordinates
     */
    @Override
    public PointCh pointAt(double position) {
        double positionMinusSegments = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.pointAt(positionMinusSegments);
            } else {
                positionMinusSegments -= segment.length();
            }
        }
        return pointAt(length());
    }

    /**
     * Returns the altitude of the point at the given position on the itinerary.
     *
     * @param position the position of the point
     * @return the elevation
     */
    @Override
    public double elevationAt(double position) {
        double positionMinusSegments = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.elevationAt(positionMinusSegments);
            } else {
                positionMinusSegments -= segment.length();
            }
        }
        return elevationAt(length());
    }

    /**
     * Returns the index of the node that belongs to the itinerary which is closest to the given position.
     *
     * @param position the position
     * @return the index of the node
     */
    @Override
    public int nodeClosestTo(double position) {
        double positionMinusSegments = Math2.clamp(0, position, length());
        for (Route segment : segments) {
            if (positionMinusSegments <= segment.length()) {
                return segment.nodeClosestTo(positionMinusSegments);
            } else {
                positionMinusSegments -= segment.length();
            }
        }
        return nodeClosestTo(length());
    }

    /**
     * Returns the point in the itinerary which is closest to the given reference point.
     *
     * @param point the reference point
     * @return the closest point in the itinerary
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint closestPoint = RoutePoint.NONE;
        double globalPosition = 0;

        for (int i = 0; i < segments.size(); i++) {
            if (i != 0)
                globalPosition += segments.get(i - 1).length();
            Route segment = segments.get(i);
            RoutePoint testPoint = segment.pointClosestTo(point)
                                          .withPositionShiftedBy(globalPosition);
            closestPoint = closestPoint.min(testPoint);
        }
        return closestPoint;
    }
}
