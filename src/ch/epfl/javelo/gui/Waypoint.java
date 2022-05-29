package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a waypoint on an itinerary.
 *
 * @author Timo Moebel (345665)
 *
 * @param position      the position of the point
 * @param closestNodeId the index of the closest node to the waypoint
 */
public record Waypoint(PointCh position, int closestNodeId) {
}
