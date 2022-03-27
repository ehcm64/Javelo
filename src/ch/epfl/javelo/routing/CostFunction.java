package ch.epfl.javelo.routing;

/**
 * Represents a cost function.
 *
 * @author Edouard Mignan (345875)
 */
public interface CostFunction {
    /**
     * Returns the factor by which to multiply the length of the
     * given edge starting from the given node.
     *
     * @param nodeId the index of the node
     * @param edgeId the index of the edge
     * @return the cost factor associated to the edge
     */
    double costFactor(int nodeId, int edgeId);
}
