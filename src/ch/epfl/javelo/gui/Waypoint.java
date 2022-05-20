package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
/**
 * Represents a way point
 * @param position the position of the way point
 *                 in Swiss coordinates
 * @param closestNodeId the id of the closest node
 * @author Timo Moebel (345665)
 */
public record Waypoint(PointCh position, int closestNodeId) {}
