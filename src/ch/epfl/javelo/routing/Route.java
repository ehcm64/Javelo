package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Represents an itinerary.
 *
 * @author Edouard Mignan (345875)
 */
public interface Route {

    /**
     * Returns the index of the segment at the given position.
     *
     * @param position the position
     * @return the index of the segment
     */
    int indexOfSegmentAt(double position);

    /**
     * Returns the length of the itinerary in meters.
     *
     * @return the length of the itinerary in meters
     */
    double length();

    /**
     * Returns the entirety of edges in the itinerary.
     *
     * @return the list of edges
     */
    List<Edge> edges();

    /**
     * Returns all the points located at the extremities of the edges
     * of the itinerary.
     *
     * @return the list of points
     */
    List<PointCh> points();

    /**
     * Returns the point in swiss coordinates at the given position in the itinerary.
     *
     * @param position the position of the point
     * @return the point in swiss coordinates
     */
    PointCh pointAt(double position);

    /**
     * Returns the index of the node that belongs to the itinerary which is closest to the given position.
     *
     * @param position the position
     * @return the index of the node
     */
    int nodeClosestTo(double position);

    /**
     * Returns the point in the itinerary which is closest to the given reference point.
     *
     * @param point the reference point
     * @return the closest point in the itinerary
     */
    RoutePoint pointClosestTo(PointCh point);

    /**
     * Returns the altitude of the point at the given position on the itinerary.
     *
     * @param position the position of the point
     * @return the elevation
     */
    double elevationAt(double position);
}
