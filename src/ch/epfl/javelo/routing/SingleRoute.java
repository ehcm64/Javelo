package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
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
        double clamp = Math2.clamp(0, position, this.length());
        int index = Arrays.binarySearch(this.nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.fromPoint();
        if (index > 0) {
            return this.edges.get(index - 1).toPoint();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = clamp - this.nodePositions[indexOfNode];
            return this.edges.get(indexOfNode).pointAt(positionAlongEdge);
        }
    }

    @Override
    public double elevationAt(double position) {
        double clamp = Math2.clamp(0, position, this.length());
        int index = Arrays.binarySearch(this.nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.elevationAt(0);
        if (index > 0) {
            Edge edge = this.edges.get(index - 1);
            return edge.elevationAt(edge.length());
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - this.nodePositions[indexOfNode];
            return this.edges.get(indexOfNode).elevationAt(positionAlongEdge);
        }
    }

    @Override
    public int nodeClosestTo(double position) {
        double clamp = Math2.clamp(0, position, this.length());
        int index = Arrays.binarySearch(nodePositions, clamp);
        if (index == 0)
            return FIRST_EDGE.fromNodeId();
        if (index > 0) {
            return this.edges.get(index - 1).toNodeId();
        } else {
            int indexOfNode = -index - 2;
            double positionAlongEdge = position - nodePositions[indexOfNode];
            double ratio = positionAlongEdge / this.edges.get(indexOfNode).length();
            return ratio <= 0.5 ? this.edges.get(indexOfNode).fromNodeId()
                    : this.edges.get(indexOfNode).toNodeId();
        }
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double minDistance = Double.POSITIVE_INFINITY;
        double edgePosition = 0;
        double totalPosition = 0;
        double positionAlongEdge, testDistance;
        Edge bestEdge = null;

        for (int i = 0; i < this.edges.size(); i++) {
            Edge edge = this.edges.get(i);
            positionAlongEdge = Math2.clamp(0,
                    edge.positionClosestTo(point),
                    edge.length());
            testDistance = point.squaredDistanceTo(
                    edge.pointAt(positionAlongEdge));
            if (testDistance < minDistance) {
                bestEdge = edge;
                minDistance = testDistance;
                edgePosition = positionAlongEdge;
                totalPosition = this.nodePositions[i] + positionAlongEdge;
            }
        }
        return new RoutePoint(bestEdge.pointAt(edgePosition),
                totalPosition,
                Math.sqrt(minDistance));
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
