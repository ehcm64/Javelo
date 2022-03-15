package ch.epfl.javelo.data;

import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphTest {

    @Test
    void loadFromWorks() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);
        assertEquals(638037 / 3, testGraph.nodeCount());
    }

    @Test
    void allGettersWork() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);
        assertEquals(638037 / 3, testGraph.nodeCount());
    }

    @Test
    void nodeClosestTo() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);
        PointCh rolex = new PointCh(2_533_132, 1_152_206);
        int closestNode = testGraph.nodeClosestTo(rolex, 20);
        System.out.println(closestNode);
        PointCh closestPoint = testGraph.nodePoint(closestNode);
        System.out.println("e : " + closestPoint.e() + "   n : " + closestPoint.n());
    }

    @Test
    void gettingAFileWorks() throws IOException {
        Path filePath = Path.of("lausanne/nodes_osmid.bin");
        LongBuffer osmIdBuffer;
        try (FileChannel channel = FileChannel.open(filePath)) {
            osmIdBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asLongBuffer();
        }
        assertEquals(310876657, osmIdBuffer.get(2022));
    }
}