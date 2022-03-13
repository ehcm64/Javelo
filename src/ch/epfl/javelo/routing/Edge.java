package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
                   DoubleUnaryOperator profile) {

    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId), graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }

    public double positionClosestTo(PointCh point) {
        double aX = fromPoint.e();
        double aY = fromPoint.n();
        double bX = toPoint.e();
        double bY = toPoint.n();
        double pX = point.e();
        double pY = point.n();

        return Math2.projectionLength(aX, aY, bX, bY, pX, pY);


    }

    public PointCh pointAt(double position) {
        double aX = fromPoint.e();
        double aY = fromPoint.n();
        double bX = toPoint.e();
        double bY = toPoint.n();

        double diffX = aX - bX;
        double diffY = aY - bY;
        double angle = Math.atan2(diffY, diffX);
        double pX = aX + Math.cos(angle) * position;
        double pY = aY + Math.sin(angle) * position;

        return new PointCh(pX, pY);

    }

    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
