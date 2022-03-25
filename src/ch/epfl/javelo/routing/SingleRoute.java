package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final Edge LAST_EDGE;

    /**
     * Creates a single route from a given list of edges.
     *
     * @param edges the list of edges
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(edges.size() != 0);
        this.edges = List.copyOf(edges);
        this.nodePositions = getNodePositions();
        this.FIRST_EDGE = this.edges.get(0);
        this.LAST_EDGE = this.edges.get(this.edges.size() - 1);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        double totalLength = 0;
        for (Edge edge : this.edges) {
            totalLength += edge.length();
        }
        return totalLength;
    }

    @Override
    public List<Edge> edges() {
        return this.edges;
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        points.add(FIRST_EDGE.fromPoint());
        for (Edge edge : this.edges) {
            points.add(edge.toPoint());
        }
        return points;
    }

    @Override
    public PointCh pointAt(double position) {
        if (position <= 0)
            return FIRST_EDGE.fromPoint();
        if (position > this.length())
            return LAST_EDGE.toPoint();

        int index = Arrays.binarySearch(this.nodePositions, position);
        if (index > 0) {
            return this.edges.get(index - 1).toPoint();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - this.nodePositions[indexOfNode];
            return this.edges.get(indexOfNode).pointAt(positionAlongEdge);
        }
    }

    @Override
    public double elevationAt(double position) {
        if (position <= 0)
            return FIRST_EDGE.elevationAt(0);
        if (position >= this.length())
            return LAST_EDGE.elevationAt(LAST_EDGE.length());

        int index = Arrays.binarySearch(this.nodePositions, position);
        if (index > 0) {
            return this.edges.get(index).elevationAt(0);
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - this.nodePositions[indexOfNode];
            return this.edges.get(indexOfNode).elevationAt(positionAlongEdge);
        }
    }

    @Override
    public int nodeClosestTo(double position) {
        if (position <= 0)
            return FIRST_EDGE.fromNodeId();
        if (position >= this.length())
            return LAST_EDGE.toNodeId();

        int index = Arrays.binarySearch(nodePositions, position);
        if (index > 0) {
            return this.edges.get(index - 1).toNodeId();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - nodePositions[indexOfNode];
            double ratio = positionAlongEdge / this.edges.get(indexOfNode).length();
            return ratio <= 0.5 ? this.edges.get(indexOfNode).fromNodeId() : this.edges.get(indexOfNode).toNodeId();
        }
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        PointCh[] closestPoints = new PointCh[this.edges.size()];

        for (int i = 0; i < this.edges.size(); i++) {
            Edge edge = this.edges.get(i);
            PointCh closestPoint = edge.pointAt(edge.positionClosestTo(point));
            closestPoints[i] = closestPoint;
        }

        PointCh closestPoint = closestPoints[0];
        double lengthOfEdges = 0;
        double positionOfPoint = 0;
        double squaredDistanceToReference = 0;

        for (int i = 0; i < closestPoints.length; i++) {
            if (i != 0)
                lengthOfEdges += this.edges.get(i - 1).length();
            if (point.squaredDistanceTo(closestPoints[i]) <= point.squaredDistanceTo(closestPoint)) {
                closestPoint = closestPoints[i];
                positionOfPoint = lengthOfEdges + this.edges.get(i).positionClosestTo(closestPoint);
                squaredDistanceToReference = point.squaredDistanceTo(closestPoints[i]);
            }
        }
        return new RoutePoint(closestPoint, positionOfPoint, Math.sqrt(squaredDistanceToReference));
    }

    private double[] getNodePositions() {
        double totalLength = 0;
        double[] nodePositions = new double[this.edges.size() + 1];
        nodePositions[0] = totalLength;

        for (int i = 1; i < nodePositions.length; i++) {
            totalLength += this.edges.get(i - 1).length();
            nodePositions[i] = totalLength;
        }
        return nodePositions;
    }
}
