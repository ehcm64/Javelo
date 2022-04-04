package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
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
        // Initialisation
        Preconditions.checkArgument(startNodeId != endNodeId);
        float[] distances = new float[graph.nodeCount()];
        int[] predecessors = new int[graph.nodeCount()];
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();

        for (int nodeId = 0; nodeId < graph.nodeCount(); nodeId++) {
            distances[nodeId] = Float.POSITIVE_INFINITY;
            predecessors[nodeId] = 0;
        }
        distances[0] = 0f;
        float startToEndDistance = (float) graph.nodePoint(startNodeId)
                .distanceTo(graph.nodePoint(endNodeId));
        exploring.add(new WeightedNode(startNodeId, 0f, startToEndDistance));

        // Node exploration loop
        while (exploring.size() != 0) {
            WeightedNode node = exploring.remove();
            distances[node.nodeId] = Float.NEGATIVE_INFINITY;
            // End node found
            if (node.nodeId == endNodeId) {
                List<Integer> routeNodes = getRouteNodes(predecessors,
                        endNodeId,
                        startNodeId);
                List<Edge> edges = getRouteEdges(routeNodes);
                return new SingleRoute(edges);
            }
            // Exploration of all the nodes' edges
            int nbOfEdges = graph.nodeOutDegree(node.nodeId);
            for (int edgeIndex = 0; edgeIndex < nbOfEdges; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, edgeIndex);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);
                float distance = (float) (node.startDistance
                        + this.costFunction.costFactor(node.nodeId, edgeId)
                        * graph.edgeLength(edgeId));
                if (distance < distances[arrivalNodeId]) {
                    distances[arrivalNodeId] = distance;
                    predecessors[arrivalNodeId] = node.nodeId;
                    float distanceToEnd = (float) graph.nodePoint(arrivalNodeId)
                            .distanceTo(graph.nodePoint(endNodeId));
                    exploring.add(new WeightedNode(arrivalNodeId, distance, distanceToEnd));
                }
            }
        }
        return null;
    }

    private List<Integer> getRouteNodes(int[] predecessors, int endNodeId, int startNodeId) {
        List<Integer> routeNodes = new ArrayList<>();
        routeNodes.add(endNodeId);
        int nodeId = endNodeId;
        while (nodeId != startNodeId) {
            int predecessor = predecessors[nodeId];
            routeNodes.add(predecessor);
            nodeId = predecessor;
        }
        Collections.reverse(routeNodes);
        return routeNodes;
    }

    private List<Edge> getRouteEdges(List<Integer> routeNodes) {
        List<Edge> edges = new ArrayList<>();
        for (int nodeId = 0; nodeId < routeNodes.size() - 1; nodeId++) {
            int fromNodeId = routeNodes.get(nodeId);
            int toNodeId = routeNodes.get(nodeId + 1);
            PointCh fromPoint = graph.nodePoint(fromNodeId);
            PointCh toPoint = graph.nodePoint(toNodeId);
            int edgeId = 0;
            for (int i = 0; i < graph.nodeOutDegree(fromNodeId); i++) {
                edgeId = graph.nodeOutEdgeId(fromNodeId, i);
                if (graph.edgeTargetNodeId(edgeId) == toNodeId)
                    break;
            }
            double length = graph.edgeLength(edgeId);
            DoubleUnaryOperator profile = graph.edgeProfile(edgeId);
            edges.add(new Edge(fromNodeId, toNodeId, fromPoint, toPoint, length, profile));
        }
        return edges;
    }

    record WeightedNode(int nodeId, float startDistance,
                        float endDistance) implements Comparable<WeightedNode> {

        @Override
        public int compareTo(WeightedNode that) {
            return Float.compare(this.startDistance + this.endDistance
                    , that.startDistance + that.endDistance);
        }
    }
}
