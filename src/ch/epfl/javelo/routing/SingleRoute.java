package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a simple itinerary, i.e. an itinerary from a start point to an end point
 * with no intermediary points.
 *
 * @author Timo Moebel (345665) and Edouard Mignan (345875)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;
    private final double[] nodePositions;
    private final Edge FIRST_EDGE;

    private final List<PointCh> points;

    /**
     * Creates a single route from a given list of edges.
     *
     * @param edges the list of edges
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        this.nodePositions = getNodePositions();
        this.FIRST_EDGE = this.edges.get(0);

        List<PointCh> pointsList = new ArrayList<>();
        pointsList.add(FIRST_EDGE.fromPoint());
        for (Edge edge : this.edges) {
            pointsList.add(edge.toPoint());
        }

        points = List.copyOf(pointsList);
    }

    /**
     * Returns the index of the segment at the given position.
     *
     * @param position the position
     * @return the index of the segment
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Returns the length of the itinerary in meters.
     *
     * @return the length of the itinerary in meters
     */
    @Override
    public double length() {
        return nodePositions[nodePositions.length - 1];
    }

    /**
     * Returns the entirety of edges in the itinerary.
     *
     * @return the list of edges
     */
    @Override
    public List<Edge> edges() {
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
        double clamp = Math2.clamp(0, position, length());
        int index = Arrays.binarySearch(nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.fromPoint();
        if (index > 0) {
            return edges.get(index - 1).toPoint();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = clamp - nodePositions[indexOfNode];
            return edges.get(indexOfNode).pointAt(positionAlongEdge);
        }
    }

    /**
     * Returns the altitude of the point at the given position on the itinerary.
     *
     * @param position the position of the point
     * @return the elevation
     */
    @Override
    public double elevationAt(double position) {
        double clamp = Math2.clamp(0, position, length());
        int index = Arrays.binarySearch(nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.elevationAt(0);
        if (index > 0) {
            Edge edge = edges.get(index - 1);
            return edge.elevationAt(edge.length());
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - nodePositions[indexOfNode];
            return edges.get(indexOfNode).elevationAt(positionAlongEdge);
        }
    }

    /**
     * Returns the index of the node that belongs to the itinerary which is closest to the given position.
     *
     * @param position the position
     * @return the index of the node
     */
    @Override
    public int nodeClosestTo(double position) {
        double clamp = Math2.clamp(0, position, length());
        int index = Arrays.binarySearch(nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.fromNodeId();
        if (index > 0) {
            return edges.get(index - 1).toNodeId();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - nodePositions[indexOfNode];
            double ratio = positionAlongEdge / edges.get(indexOfNode).length();
            return ratio <= 0.5 ? edges.get(indexOfNode).fromNodeId()
                    : edges.get(indexOfNode).toNodeId();
        }
    }

    /**
     * Returns the point in the itinerary which is closest to the given reference point.
     *
     * @param point the reference point
     * @return the closest point in the itinerary
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double absolutePosition;
        double positionAlongEdge;
        double testDistance;
        RoutePoint closestPoint = RoutePoint.NONE;

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            positionAlongEdge = Math2.clamp(0,
                    edge.positionClosestTo(point),
                    edge.length());
            PointCh testPoint = edge.pointAt(positionAlongEdge);
            testDistance = point.distanceTo(testPoint);
            absolutePosition = nodePositions[i] + positionAlongEdge;
            closestPoint = closestPoint.min(testPoint, absolutePosition, testDistance);
        }
        return closestPoint;
    }

    private double[] getNodePositions() {
        double totalLength = 0;
        double[] nodePositions = new double[edges.size() + 1];
        nodePositions[0] = totalLength;

        for (int i = 1; i < nodePositions.length; i++) {
            totalLength += edges.get(i - 1).length();
            nodePositions[i] = totalLength;
        }
        return nodePositions;
    }
}
