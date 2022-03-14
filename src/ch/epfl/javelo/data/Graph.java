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

/**
 * Represents the JaVelo Graph.
 *
 * @author Edouard Mignan (345875)
 */
public class Graph {
    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Creates a graph from nodes, sectors, edges and a list of attribute sets.
     *
     * @param nodes         the nodes of the graph
     * @param sectors       the sectors of the graph
     * @param edges         the edges of the graph
     * @param attributeSets the attribute sets of the edges of the graph
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
        this.nodes = new GraphNodes(nodes.buffer());
        this.sectors = new GraphSectors(sectors.buffer());
        this.edges = new GraphEdges(edges.edgesBuffer(), edges.profileIds(), edges.elevations());
        this.attributeSets = new ArrayList<>();
        this.attributeSets.addAll(attributeSets);
    }

    /**
     * Creates a JaVelo Graph from provided files containing nodes, sectors, edges, profileIds, elevations and attributes.
     *
     * @param basePath the path to the files
     * @return the graph built from these files
     * @throws IOException if file missing
     */
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

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of nodes
     */
    public int nodeCount() {
        return this.nodes.count();
    }

    /**
     * Returns the point (in swiss coordinates) associated with a node.
     *
     * @param nodeId the index of the node
     * @return the point associated to the node
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(this.nodes.nodeE(nodeId), this.nodes.nodeN(nodeId));
    }

    /**
     * Returns the number of edges coming out of a node.
     *
     * @param nodeId the index of the node
     * @return the number of edges
     */
    public int nodeOutDegree(int nodeId) {
        return this.nodes.outDegree(nodeId);
    }

    /**
     * Returns the "global" index of an edge coming out of a given node.
     *
     * @param nodeId    the index of the node
     * @param edgeIndex the "local" index of the edge (restricted to edges coming out of the node)
     * @return the index of the edge
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return this.nodes.edgeId(nodeId, edgeIndex);
    }

    //TODO : Refaire la méthode en ne prenant que les noeuds de secteurs dans le searchdistance.

    /**
     * Returns the index of the node closest to a given point.
     *
     * @param point          the point in swiss coordinates
     * @param searchDistance the maximum search distance around the point
     * @return the index of the closest node, or -1 if there is no node
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        PointCh comparisonPoint = new PointCh(point.e() - searchDistance, point.n() - searchDistance);
        int closestAcceptableNodeId = -1;
        List<GraphSectors.Sector> sectorsInArea = sectors.sectorsInArea(point, searchDistance);
        for (GraphSectors.Sector sector : sectorsInArea) {
            for (int nodeId = sector.startNodeId(); nodeId <= sector.endNodeId(); nodeId++) {
                PointCh nodePoint = new PointCh(this.nodes.nodeE(nodeId), this.nodes.nodeN(nodeId));
                double testNodeDistanceSquared = point.squaredDistanceTo(nodePoint);
                double comparisonDistanceSquared = point.squaredDistanceTo(comparisonPoint);
                if (testNodeDistanceSquared <= comparisonDistanceSquared) {
                    closestAcceptableNodeId = nodeId;
                    comparisonPoint = nodePoint;
                }
            }
        }
        return closestAcceptableNodeId;
    }

    /**
     * Returns the index of the target node of a given edge.
     *
     * @param edgeId the index of the edge
     * @return the index of the target node
     */
    public int edgeTargetNodeId(int edgeId) {
        return this.edges.targetNodeId(edgeId);
    }

    /**
     * Checks if a given edge is inverted.
     *
     * @param edgeId the index of the edge
     * @return a boolean : true if the edge is inverted - false otherwise
     */
    public boolean edgeIsInverted(int edgeId) {
        return this.edges.isInverted(edgeId);
    }

    /**
     * Returns the attribute set associated with a given edge.
     *
     * @param edgeId the index of the edge
     * @return the attribute set associated to the edge
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return this.attributeSets.get(this.edges.attributesIndex(edgeId));
    }

    /**
     * Returns the length of a given edge
     *
     * @param edgeId the index of the edge
     * @return the length of the edge
     */
    public double edgeLength(int edgeId) {
        return this.edges.length(edgeId);
    }

    /**
     * Returns the elevation gain of a given edge.
     *
     * @param edgeId the index of the edge
     * @return the elevation gain of the edge
     */
    public double edgeElevationGain(int edgeId) {
        return this.edges.elevationGain(edgeId);
    }

    /**
     * Returns the elevation profile of an edge in the form of a function.
     *
     * @param edgeId the index of the edge
     * @return the elevation profile
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        double length = this.edges.length(edgeId);
        float[] profileSamples = this.edges.profileSamples(edgeId);
        boolean hasProfile = this.edges.hasProfile(edgeId);
        return hasProfile ? Functions.sampled(profileSamples, length) : Functions.constant(Double.NaN);
    }
}