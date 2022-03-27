package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;

/**
 * Represents an itinerary planner.
 *
 * @author Edouard Mignan (345875) and Timo Moebel (345665)
 */
public final class RouteComputer {

    /**
     * Creates a route planner from the given graph and cost function.
     *
     * @param graph        the graph containing nodes and edges
     * @param costFunction the cost function
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {

    }

    /**
     * Returns the itinerary with minimum cost from the given starting node to the given end node.
     *
     * @param startNodeId the index of the starting node
     * @param endNodeId   the index of the end node
     * @return the best route between the two nodes
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        return null;
    }
}
