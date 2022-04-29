package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;

/**
 * Represents a collection of all Javelo's graph's nodes.
 *
 * @author Timo Moebel (345665)
 */
public record GraphNodes(IntBuffer buffer) {
    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;

    private static final int OUT_DEGREE_START = 28;
    private static final int OUT_DEGREE_LENGTH = 4;
    private static final int FIRST_EDGE_ID_START = 0;
    private static final int FIRST_EDGE_ID_LENGTH = 28;

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of nodes
     */
    public int count() {
        return buffer.capacity() / NODE_INTS;
    }

    /**
     * Returns a given node's east PointCh Coordinate.
     *
     * @param nodeId the index of the node
     * @return the east coordinate of the node
     */
    public double nodeE(int nodeId) {
        int extracted_e = buffer.get(nodeId * NODE_INTS + OFFSET_E);
        return Q28_4.asDouble(extracted_e);
    }

    /**
     * Returns a given node's north PointCh coordinate.
     *
     * @param nodeId the index of the node
     * @return the north coordinate of the node
     */
    public double nodeN(int nodeId) {
        int extracted_n = buffer.get(nodeId * NODE_INTS + OFFSET_N);
        return Q28_4.asDouble(extracted_n);
    }

    /**
     * Returns the number of edges coming out of a given node.
     *
     * @param nodeId the index of the node
     * @return the number of edges
     */
    public int outDegree(int nodeId) {
        int nodeEdgesInfo = buffer.get(nodeId * NODE_INTS + OFFSET_OUT_EDGES);
        return Bits.extractUnsigned(
                nodeEdgesInfo,
                OUT_DEGREE_START,
                OUT_DEGREE_LENGTH);
    }

    /**
     * Returns the "global" index of an edge coming out of a given node.
     *
     * @param nodeId    the index of the node
     * @param edgeIndex the "local" index of the edge (restricted to edges coming out of the node)
     * @return the index of the edge
     */
    public int edgeId(int nodeId, int edgeIndex) {
        Preconditions.checkArgument(edgeIndex >= 0
                && edgeIndex < outDegree(nodeId));
        int nodeEdgesInfo = buffer.get(nodeId * NODE_INTS + OFFSET_OUT_EDGES);
        int firstEdgeId = Bits.extractUnsigned(
                nodeEdgesInfo,
                FIRST_EDGE_ID_START,
                FIRST_EDGE_ID_LENGTH);
        return firstEdgeId + edgeIndex;
    }
}
