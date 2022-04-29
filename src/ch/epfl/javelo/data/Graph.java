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
public final class Graph {
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
    public Graph(GraphNodes nodes,
                 GraphSectors sectors,
                 GraphEdges edges,
                 List<AttributeSet> attributeSets) {

        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    private static ByteBuffer getBuffer(Path basePath) throws IOException {
        try (FileChannel channel = FileChannel.open(basePath)) {
            return channel.map(FileChannel.MapMode.READ_ONLY,
                    0, channel.size()).asReadOnlyBuffer();
        }
    }

    /**
     * Creates a JaVelo Graph from provided files containing nodes, sectors, edges, profileIds, elevations and attributes.
     *
     * @param basePath the path to the files
     * @return the graph built from these files
     * @throws IOException if there is a problem with a file or the path
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        Path nodesPath = basePath.resolve("nodes.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path profile_idsPath = basePath.resolve("profile_ids.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path sectorsPath = basePath.resolve("sectors.bin");
        Path attributesPath = basePath.resolve("attributes.bin");

        IntBuffer nodesBuffer = getBuffer(nodesPath).asIntBuffer();
        IntBuffer profilesBuffer = getBuffer(profile_idsPath).asIntBuffer();
        ByteBuffer edgesBuffer = getBuffer(edgesPath).asReadOnlyBuffer();
        ByteBuffer sectorsBuffer = getBuffer(sectorsPath).asReadOnlyBuffer();
        ShortBuffer elevationsBuffer = getBuffer(elevationsPath).asShortBuffer();
        LongBuffer attributeSetsBuffer = getBuffer(attributesPath).asLongBuffer();

        List<AttributeSet> attributeSets = new ArrayList<>(attributeSetsBuffer.capacity());

        for (int i = 0; i < attributeSetsBuffer.capacity(); i++) {
            attributeSets.add(new AttributeSet(attributeSetsBuffer.get(i)));
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
        return nodes.count();
    }

    /**
     * Returns the point (in swiss coordinates) associated with a node.
     *
     * @param nodeId the index of the node
     * @return the point associated to the node
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Returns the number of edges coming out of a node.
     *
     * @param nodeId the index of the node
     * @return the number of edges
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Returns the "global" index of an edge coming out of a given node.
     *
     * @param nodeId    the index of the node
     * @param edgeIndex the "local" index of the edge (restricted to edges coming out of the node)
     * @return the index of the edge
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Returns the index of the node closest to a given point.
     *
     * @param point          the point in swiss coordinates
     * @param searchDistance the maximum search startDistance around the point
     * @return the index of the closest node, or -1 if there is no node
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        double smallestDistanceYet = searchDistance * searchDistance;
        int closestAcceptableNodeId = -1; // invalid node ID if there is no closest node.
        List<GraphSectors.Sector> sectorsInArea = sectors.sectorsInArea(point, searchDistance);
        for (GraphSectors.Sector sector : sectorsInArea) {
            for (int nodeId = sector.startNodeId(); nodeId < sector.endNodeId(); nodeId++) {
                PointCh nodePoint = nodePoint(nodeId);
                double testDistance = point.squaredDistanceTo(nodePoint);
                if (testDistance <= smallestDistanceYet) {
                    closestAcceptableNodeId = nodeId;
                    smallestDistanceYet = testDistance;
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
        return edges.targetNodeId(edgeId);
    }

    /**
     * Checks if a given edge is inverted.
     *
     * @param edgeId the index of the edge
     * @return a boolean : true if the edge is inverted - false otherwise
     */
    public boolean edgeIsInverted(int edgeId) {
        return edges.isInverted(edgeId);
    }

    /**
     * Returns the attribute set associated with a given edge.
     *
     * @param edgeId the index of the edge
     * @return the attribute set associated to the edge
     */
    public AttributeSet edgeAttributes(int edgeId) {
        int attributesIndex = edges.attributesIndex(edgeId);
        return attributeSets.get(attributesIndex);
    }

    /**
     * Returns the length of a given edge
     *
     * @param edgeId the index of the edge
     * @return the length of the edge
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Returns the elevation gain of a given edge.
     *
     * @param edgeId the index of the edge
     * @return the elevation gain of the edge
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Returns the elevation profile of an edge in the form of a function.
     *
     * @param edgeId the index of the edge
     * @return the elevation profile
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        double length = edges.length(edgeId);
        boolean hasProfile = edges.hasProfile(edgeId);
        float[] profileSamples = edges.profileSamples(edgeId);
        return hasProfile ? Functions.sampled(profileSamples, length)
                : Functions.constant(Double.NaN);
    }
}