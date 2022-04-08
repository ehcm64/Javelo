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
        Preconditions.checkArgument(startNodeId != endNodeId);

        record WeightedNode(int nodeId, float sumDistance)
                implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.sumDistance, that.sumDistance);
            }
        }
        // Initialization
        float[] distances = new float[NB_OF_NODES]; // contains path lengths to each node from start
        int[] predecessors = new int[NB_OF_NODES]; // contains node's predecessor in route
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        for (int nodeId = 0; nodeId < graph.nodeCount(); nodeId++) {
            distances[nodeId] = Float.POSITIVE_INFINITY;
        }
        distances[startNodeId] = 0f;
        exploring.add(new WeightedNode(startNodeId, 0));

        // Node exploration loop
        while (exploring.size() != 0) {
            WeightedNode node = exploring.remove();
            // if node has already been explored
            if (distances[node.nodeId] == Float.NEGATIVE_INFINITY) {
                continue;
            }
            // End node found
            if (node.nodeId == endNodeId)
                return getRoute(predecessors, startNodeId, endNodeId);
            // Exploration of all the nodes' edges
            int nbOfEdges = graph.nodeOutDegree(node.nodeId);
            float pathToNodeLength = distances[node.nodeId];
            for (int edgeIndex = 0; edgeIndex < nbOfEdges; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(node.nodeId, edgeIndex);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);
                // if arrival node has already been explored
                if (distances[arrivalNodeId] == Float.NEGATIVE_INFINITY)
                    continue;

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

    private Route getRoute(int[] predecessors, int startNodeId, int endNodeId) {
        List<Edge> edges = new ArrayList<>();
        // Iterate through all the route's nodes from end to start
        int nodeId = endNodeId;
        while (nodeId != startNodeId) {
            int predecessor = predecessors[nodeId];
            PointCh fromPoint = graph.nodePoint(predecessor);
            PointCh toPoint = graph.nodePoint(nodeId);
            int edgeId = 0;
            // iterate through all the current node's edges
            // until the one we want is found
            for (int i = 0; i < graph.nodeOutDegree(predecessor); i++) {
                edgeId = graph.nodeOutEdgeId(predecessor, i);
                if (graph.edgeTargetNodeId(edgeId) == nodeId)
                    break;
            }
            double length = graph.edgeLength(edgeId);
            DoubleUnaryOperator profile = graph.edgeProfile(edgeId);
            edges.add(new Edge(predecessor,nodeId, fromPoint, toPoint, length, profile));
            nodeId = predecessor;
        }
        // reverse the list to get the route's edges in correct order
        Collections.reverse(edges);
        return new SingleRoute(edges);
    }
}
