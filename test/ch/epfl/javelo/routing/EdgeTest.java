package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeTest {

    @Test
    void pointAtWorks() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);
        System.out.println("edge length : " + testGraph.edgeLength(0));
        System.out.println("edge To Node : " + testGraph.edgeTargetNodeId(0));
        int toNode = testGraph.edgeTargetNodeId(0);

        Edge edge = Edge.of(testGraph, 0, 0, 1);
        double edgeVector_E = edge.toPoint().e() - edge.fromPoint().e();
        double edgeVector_N = edge.toPoint().n() - edge.fromPoint().n();

        PointCh testPoint = new PointCh(edge.fromPoint().e() + edgeVector_E * (50 / edge.length()), edge.fromPoint().n() + edgeVector_N * (50 / edge.length()));
        assertEquals(testPoint.e(), edge.pointAt(50).e(), 1e-1);
        assertEquals(testPoint.n(), edge.pointAt(50).n(), 1e-1);
    }

}