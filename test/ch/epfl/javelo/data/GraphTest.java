package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphTest {

    @Test
    void loadFromWorks() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);

        IntBuffer nodesBuffer, profilesBuffer;
        ByteBuffer edgesBuffer, sectorsBuffer;
        ShortBuffer elevationsBuffer;
        List<AttributeSet> attributeSets;

        Path nodesPath = basePath.resolve("nodes.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path profile_idsPath = basePath.resolve("profile_ids.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path sectorsPath = basePath.resolve("sectors.bin");
        Path attributesPath = basePath.resolve("attributes.bin");

        try (FileChannel nodesChannel = FileChannel.open(nodesPath)) {
            nodesBuffer = nodesChannel.map(FileChannel.MapMode.READ_ONLY, 0, nodesChannel.size()).asIntBuffer();
        }
        try (FileChannel sectorsChannel = FileChannel.open(sectorsPath)) {
            sectorsBuffer = sectorsChannel.map(FileChannel.MapMode.READ_ONLY, 0, sectorsChannel.size()).asReadOnlyBuffer();
        }
        try (FileChannel edgesChannel = FileChannel.open(edgesPath)) {
            edgesBuffer = edgesChannel.map(FileChannel.MapMode.READ_ONLY, 0, edgesChannel.size()).asReadOnlyBuffer();
        }
        try (FileChannel profilesChannel = FileChannel.open(profile_idsPath)) {
            profilesBuffer = profilesChannel.map(FileChannel.MapMode.READ_ONLY, 0, profilesChannel.size()).asIntBuffer();
        }
        try (FileChannel elevationsChannel = FileChannel.open(elevationsPath)) {
            elevationsBuffer = elevationsChannel.map(FileChannel.MapMode.READ_ONLY, 0, elevationsChannel.size()).asShortBuffer();
        }
        try (FileChannel attributesChannel = FileChannel.open(attributesPath)) {
            LongBuffer attributeSetsBuffer = attributesChannel.map(FileChannel.MapMode.READ_ONLY, 0, attributesChannel.size()).asLongBuffer();
            attributeSets = new ArrayList<>();
            for (int i = 0; i < attributeSetsBuffer.capacity(); i++) {
                attributeSets.add(new AttributeSet(attributeSetsBuffer.get(i)));
            }
        }

        GraphNodes nodes = new GraphNodes(nodesBuffer);
        GraphSectors sectors = new GraphSectors(sectorsBuffer);
        GraphEdges edges = new GraphEdges(edgesBuffer, profilesBuffer, elevationsBuffer);

        assertEquals(nodes.count(), testGraph.nodeCount());
        assertEquals(nodes.nodeE(5), testGraph.nodePoint(5).e());
        assertEquals(nodes.nodeN(5), testGraph.nodePoint(5).n());
        assertEquals(nodes.outDegree(5), testGraph.nodeOutDegree(5));
        assertEquals(nodes.edgeId(6, 1), testGraph.nodeOutEdgeId(6, 1));
        assertEquals(edges.targetNodeId(6), testGraph.edgeTargetNodeId(6));
        assertEquals(edges.isInverted(6), testGraph.edgeIsInverted(6));
        assertEquals(attributeSets.get(edges.attributesIndex(6)).bits(), testGraph.edgeAttributes(6).bits());
        assertEquals(edges.length(6), testGraph.edgeLength(6));
        assertEquals(edges.elevationGain(6), testGraph.edgeElevationGain(6));
        assertEquals(Functions.sampled(edges.profileSamples(0), edges.length(0)).applyAsDouble(4), testGraph.edgeProfile(0).applyAsDouble(4));
    }

    @Test
    void nodeClosestTo() throws IOException {
        Path basePath = Path.of("lausanne");
        Graph testGraph = Graph.loadFrom(basePath);
        PointCh rolex = new PointCh(2_533_132, 1_152_206);
        int closestNode = testGraph.nodeClosestTo(rolex, 11);
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