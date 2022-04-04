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
    private final int NB_OF_NODES;

    /**
     * Creates a route planner from the given graph and cost function.
     *
     * @param graph        the graph containing nodes and edges
     * @param costFunction the cost function
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
        this.NB_OF_NODES = graph.nodeCount();
    }

    /**
     * Returns the itinerary with minimum cost from the given starting node to the given end node.
     *
     * @param startNodeId the index of the starting node
     * @param endNodeId   the index of the end node
     * @return the best route between the two nodes
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        // Initialization
        Preconditions.checkArgument(startNodeId != endNodeId);
        float[] distances = new float[NB_OF_NODES];
        int[] predecessors = new int[NB_OF_NODES];
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();

        for (int nodeId = 0; nodeId < graph.nodeCount(); nodeId++) {
            distances[nodeId] = Float.POSITIVE_INFINITY;
        }
        distances[startNodeId] = 0f;

        float startToEndDistance = (float) graph.nodePoint(startNodeId)
                .distanceTo(graph.nodePoint(endNodeId));
        exploring.add(new WeightedNode(startNodeId, startToEndDistance));

        // Node exploration loop
        while (exploring.size() != 0) {
            WeightedNode node = exploring.remove();
            // if node has already been explored
            if (distances[node.nodeId] == Float.NEGATIVE_INFINITY) {
                continue;
            }

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
            float pathToNodeLength = distances[node.nodeId];
            for (int edgeIndex = 0; edgeIndex < nbOfEdges; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, edgeIndex);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);

                // if arrival node has already been explored
                if (distances[arrivalNodeId] == Float.NEGATIVE_INFINITY) {
                    continue;
                }
                float pathToArrivalNodeLength = (float) (
                        pathToNodeLength
                                + this.costFunction.costFactor(node.nodeId, edgeId)
                                * graph.edgeLength(edgeId));

                if (pathToArrivalNodeLength < distances[arrivalNodeId]) {
                    distances[arrivalNodeId] = pathToArrivalNodeLength;
                    predecessors[arrivalNodeId] = node.nodeId;
                    float distanceToEndNode = (float) graph.nodePoint(arrivalNodeId)
                            .distanceTo(graph.nodePoint(endNodeId));
                    float sumDistance = pathToArrivalNodeLength + distanceToEndNode;
                    exploring.add(new WeightedNode(arrivalNodeId, sumDistance));
                }
            }
            // mark node as explored
            distances[node.nodeId] = Float.NEGATIVE_INFINITY;
        }
        return null;
    }

    private List<Integer> getRouteNodes(int[] predecessors, int endNodeId, int startNodeId) {
        List<Integer> routeNodes = new ArrayList<>();
        // get a list of all the route's nodes from the end to the start
        routeNodes.add(endNodeId);
        int nodeId = endNodeId;
        while (nodeId != startNodeId) {
            int predecessor = predecessors[nodeId];
            routeNodes.add(predecessor);
            nodeId = predecessor;
        }
        // reverse the list to get the route's nodes in correct order
        Collections.reverse(routeNodes);
        return routeNodes;
    }

    private List<Edge> getRouteEdges(List<Integer> routeNodes) {
        int nbOfEdges = routeNodes.size() - 1;
        List<Edge> edges = new ArrayList<>();
        // iterate through all the route's nodes
        for (int node = 0; node < nbOfEdges; node++) {
            int fromNodeId = routeNodes.get(node);
            int toNodeId = routeNodes.get(node + 1);
            PointCh fromPoint = graph.nodePoint(fromNodeId);
            PointCh toPoint = graph.nodePoint(toNodeId);
            int edgeId = 0;
            // iterate through all the current node's edges
            // until the one we want is found
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

    record WeightedNode(int nodeId, float sumDistance)
            implements Comparable<WeightedNode> {

        @Override
        public int compareTo(WeightedNode that) {
            return Float.compare(this.sumDistance, that.sumDistance);
        }
    }
}
