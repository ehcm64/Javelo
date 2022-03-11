package ch.epfl.javelo.data;

import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Graph {

    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
    }

    public static Graph loadFrom(Path basePath) throws IOException {
        return null;
    }

    public int nodeCount() {
        return 0;
    }

    public PointCh nodePoint(int nodeId) {
        return null;
    }

    public int nodeOutDegree(int nodeId) {
        return 0;
    }

    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return 0;
    }

    public int nodeClosestTo(PointCh point, double searchDistance) {
        return 0;
    }

    public int edgeTargetNodeId(int edgeId) {
        return 0;
    }

    public boolean edgeIsInverted(int edgeId) {
        return false;
    }

    public AttributeSet edgeAttributes(int edgeId) {
        return null;
    }

    public double edgeLength(int edgeId) {
        return 0;
    }

    public double edgeElevationGain(int edgeId) {
        return 0;
    }

    public DoubleUnaryOperator edgeProfile(int edgeId) {
        return null;
    }
}
