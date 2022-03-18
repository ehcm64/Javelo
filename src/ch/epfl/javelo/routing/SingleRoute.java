package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple itinerary, i.e. an itinerary from a start point to an end point
 * with no intermediary points.
 *
 * @author Timo Moebel (345665) and Edouard Mignan (345875)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;

    /**
     * Creates a single route from a given list of edges.
     *
     * @param edges the list of edges
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(edges.size() != 0);
        this.edges = List.copyOf(edges);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        double totalLength = 0;
        for (Edge edge : this.edges) {
            totalLength += edge.length();
        }
        return totalLength;
    }

    @Override
    public List<Edge> edges() {
        return this.edges;
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        points.add(this.edges.get(0).fromPoint());
        for (Edge edge : this.edges) {
            points.add(edge.toPoint());
        }
        return points;
    }

    @Override
    public PointCh pointAt(double position) {
        return null;
    }

    @Override
    public double elevationAt(double position) {
        return 0;
    }

    @Override
    public int nodeClosestTo(double position) {
        return 0;
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        return null;
    }
}
