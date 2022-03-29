package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

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

        Map<Integer, Double> distances = new TreeMap<>();
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        Map<Integer, Integer> predecessors = new TreeMap<>();

        for (int nodeId = 0; nodeId < graph.nodeCount(); nodeId++) {
            distances.put(nodeId, Double.POSITIVE_INFINITY);
            predecessors.put(nodeId, 0);
        }
        distances.put(0, 0d);
        exploring.add(new WeightedNode(startNodeId, 0d));

        while (exploring.size() != 0) {
            WeightedNode node = exploring.remove();
            distances.put(node.nodeId, Double.NEGATIVE_INFINITY);

            if (node.nodeId == endNodeId) {
                List<Integer> nodes = new ArrayList<>();
                int nodeId = endNodeId;
                while (nodeId != startNodeId) {
                    int predecessor = predecessors.get(nodeId);
                    nodes.add(predecessor);
                    nodeId = predecessor;
                }
                Collections.reverse(nodes);
                List<Edge> edges = new ArrayList<>();
                for (int i = 0; i < nodes.size() - 1; i++) {
                    int fromNodeId = nodes.get(i);
                    int toNodeId = nodes.get(i + 1);
                    PointCh fromPoint = graph.nodePoint(fromNodeId);
                    PointCh toPoint = graph.nodePoint(toNodeId);
                    int edgeId = 0;
                    for (int j = 0; j < graph.nodeOutDegree(fromNodeId); j++) {
                        edgeId = graph.nodeOutEdgeId(fromNodeId, j);
                        if (graph.edgeTargetNodeId(edgeId) == toNodeId)
                            break;
                    }
                    double length = graph.edgeLength(edgeId);
                    DoubleUnaryOperator profile = graph.edgeProfile(edgeId);
                    edges.add(new Edge(fromNodeId, toNodeId, fromPoint, toPoint, length, profile));
                }
                return new SingleRoute(edges);
            }

            for (int edgeIndex = 0; edgeIndex < graph.nodeOutDegree(node.nodeId); edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, edgeIndex);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);
                double distance = node.distance + this.costFunction.costFactor(node.nodeId, edgeId) * graph.edgeLength(edgeId);
                if (distance < distances.get(arrivalNodeId)) {
                    distances.put(arrivalNodeId, distance);
                    predecessors.put(arrivalNodeId, node.nodeId);
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
