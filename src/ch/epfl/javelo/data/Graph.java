package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Graph {
    private GraphNodes nodes;
    private GraphSectors sectors;
    private GraphEdges edges;
    private List<AttributeSet> attributeSets;

    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = attributeSets;
    }

    public static Graph loadFrom(Path basePath) throws IOException {
        Path nodesPath = basePath.resolve("nodes.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path profile_idsPath = basePath.resolve("profile_ids.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path sectorsPath = basePath.resolve("sectors.bin");
        Path attributesPath = basePath.resolve("attributes.bin");

        IntBuffer nodesBuffer, profilesBuffer;
        ByteBuffer edgesBuffer, sectorsBuffer;
        ShortBuffer elevationsBuffer;
        List<AttributeSet> attributeSets;

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
            attributeSets = new ArrayList<>(attributeSetsBuffer.capacity());
            for (int i = 0; i < attributeSetsBuffer.capacity(); i++) {
                attributeSets.set(i, new AttributeSet(attributeSetsBuffer.get(i)));
            }
        }

        GraphNodes nodes = new GraphNodes(nodesBuffer);
        GraphSectors sectors = new GraphSectors(sectorsBuffer);
        GraphEdges edges = new GraphEdges(edgesBuffer, profilesBuffer, elevationsBuffer);

        return new Graph(nodes, sectors, edges, attributeSets);
    }

    public int nodeCount() {
        return this.nodes.count();
    }

    public PointCh nodePoint(int nodeId) {
        return new PointCh(this.nodes.nodeE(nodeId), this.nodes.nodeN(nodeId));
    }

    public int nodeOutDegree(int nodeId) {
        return this.nodes.outDegree(nodeId);
    }

    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return this.nodes.edgeId(nodeId, edgeIndex);
    }

    public int nodeClosestTo(PointCh point, double searchDistance) {
        return 0;
    }

    public int edgeTargetNodeId(int edgeId) {
        return this.edges.targetNodeId(edgeId);
    }

    public boolean edgeIsInverted(int edgeId) {
        return this.edges.isInverted(edgeId);
    }

    public AttributeSet edgeAttributes(int edgeId) {
        return this.attributeSets.get(this.edges.attributesIndex(edgeId));
    }

    public double edgeLength(int edgeId) {
        return this.edges.length(edgeId);
    }

    public double edgeElevationGain(int edgeId) {
        return this.edges.elevationGain(edgeId);
    }

    public DoubleUnaryOperator edgeProfile(int edgeId) {
        double length = this.edges.length(edgeId);
        float[] profileSamples = this.edges.profileSamples(edgeId);
        boolean hasProfile = this.edges.hasProfile(edgeId);
        return hasProfile ? Functions.sampled(profileSamples, length) : /*jsais pas comment faire;
    }
}
