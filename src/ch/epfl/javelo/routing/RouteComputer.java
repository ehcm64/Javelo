package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Represents an itinerary planner.
 *
 * @author Edouard Mignan (345875) and Timo Moebel (345665)
 */
public final class RouteComputer {
    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * Creates a route planner from the given graph and cost function.
     *
     * @param graph        the graph containing nodes and edges
     * @param costFunction the cost function
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Returns the itinerary with minimum cost from the given starting node to the given end node.
     *
     * @param startNodeId the index of the starting node
     * @param endNodeId   the index of the end node
     * @return the best route between the two nodes
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        List<Double> distances = new ArrayList<>();
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        List<Integer> predecessors = new ArrayList<>();

        for (int i = 0; i <= endNodeId - startNodeId; i++) {
            distances.add(Double.POSITIVE_INFINITY);
            predecessors.add(0);
        }
        distances.set(0, 0d);
        exploring.add(new WeightedNode(startNodeId, 0d));

        while (exploring.size() != 0) {
            WeightedNode node = exploring.remove();
            distances.set(node.nodeId - startNodeId, Double.NEGATIVE_INFINITY);

            if (node.nodeId == endNodeId) {
                List<Edge> edges = new ArrayList<>();
                return null;
            }

            for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, i);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);
                double distance = node.distance + graph.edgeLength(edgeId);
                if (distance < distances.get(arrivalNodeId - startNodeId)) {
                    distances.set(arrivalNodeId - startNodeId, distance);
                    predecessors.set(arrivalNodeId - startNodeId, node.nodeId);
                    exploring.add(new WeightedNode(arrivalNodeId, distance));
                }
            }
        }
        return null;
    }

    record WeightedNode(int nodeId, double distance) implements Comparable<WeightedNode> {
        @Override
        public int compareTo(WeightedNode that) {
            return Double.compare(this.distance, that.distance);
        }
    }
}
