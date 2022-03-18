package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents an edge.
 *
 * @author Timo Moebel (345665)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
                   DoubleUnaryOperator profile) {

    /**
     * Static method to create an edge from given arguments.
     *
     * @param graph      the graph in which the edge is contained
     * @param edgeId     the index of the edge in the graph
     * @param fromNodeId the index of the start node of the edge
     * @param toNodeId   the index of the arrival node of the edge
     * @return the created edge
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId), graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }

    /**
     * Return the position on the edge of the closest point to the reference point given in argument.
     *
     * @param point the reference point in swiss coordinates
     * @return the position on the edge of the closest point
     */
    public double positionClosestTo(PointCh point) {
        double aX = fromPoint.e();
        double aY = fromPoint.n();
        double bX = toPoint.e();
        double bY = toPoint.n();
        double pX = point.e();
        double pY = point.n();

        return Math2.projectionLength(aX, aY, bX, bY, pX, pY);
    }

    /**
     * Returns the point in swiss coordinates at the given position along the edge.
     *
     * @param position the position on the edge of the point
     *                 (can be negative or greater than the length of the edge)
     * @return the point in swiss coordinates
     */
    public PointCh pointAt(double position) {
        double aX = fromPoint.e();
        double aY = fromPoint.n();
        double bX = toPoint.e();
        double bY = toPoint.n();

        double diffX = bX - aX;
        double diffY = bY - aY;

        double pX = aX + diffX * (position / this.length);
        double pY = aY + diffY * (position / this.length);

        return new PointCh(pX, pY);
    }

    /**
     * Returns the elevation of a point on the edge at a given position.
     *
     * @param position the position of the point along the edge
     * @return the elevation of the point
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
