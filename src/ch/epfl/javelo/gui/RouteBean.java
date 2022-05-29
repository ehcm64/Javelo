package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a JavaFX Bean containing properties related to the route.
 *
 * @author Timo Moebel (345665)
 */
public final class RouteBean {

    private final ObservableList<Waypoint> waypoints;
    private final ObjectProperty<Route> route;
    private final ObjectProperty<ElevationProfile> elevationProfile;
    private final DoubleProperty highlightedPosition;
    private final RouteComputer routeComputer;
    private final Map<Pair<Integer, Integer>, Route> memoryCache;

    private static final int MAX_STEP_LENGTH = 5;
    private static final int CACHE_SIZE = 50;

    /**
     * Creates a route bean.
     *
     * @param routeComputer the route computer used to compute a route
     */
    public RouteBean(RouteComputer routeComputer) {

        this.elevationProfile = new SimpleObjectProperty<>();
        this.route = new SimpleObjectProperty<>();
        this.routeComputer = routeComputer;
        this.highlightedPosition = new SimpleDoubleProperty(Double.NaN);

        waypoints = FXCollections.observableArrayList();

        memoryCache = new LinkedHashMap<>(CACHE_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };

        waypoints.addListener((ListChangeListener<? super Waypoint>) e -> buildRoute());

        route.addListener(p -> buildElevationProfile());
    }

    private void buildElevationProfile() {
        if (route.get() == null) {
            elevationProfile.set(null);
        } else {
            elevationProfile.set(
                    ElevationProfileComputer.elevationProfile(
                            route.get(),
                            MAX_STEP_LENGTH));
        }
    }

    private void buildRoute() {
        if (waypoints.size() >= 2) {
            List<Route> singleRoutes = new ArrayList<>();
            for (int i = 0; i < waypoints.size() - 1; i++) {
                int startNodeId = waypoints.get(i).closestNodeId();
                int endNodeId = waypoints.get(i + 1).closestNodeId();
                if (startNodeId == endNodeId) continue;
                Pair<Integer, Integer> pair = new Pair<>(startNodeId, endNodeId);
                boolean needToCalculateRoute = true;

                for (Pair<Integer, Integer> p : memoryCache.keySet()) {
                    if (p.equals(pair)) {
                        singleRoutes.add(memoryCache.get(p));
                        needToCalculateRoute = false;
                        break;
                    }
                }
                if (needToCalculateRoute) {
                    Route singleRoute = routeComputer.bestRouteBetween(startNodeId, endNodeId);
                    if (singleRoute == null) {
                        route.set(null);
                        return;
                    }
                    memoryCache.put(pair, singleRoute);
                    singleRoutes.add(singleRoute);
                }
            }
            if (!singleRoutes.isEmpty()) {
                route.set(new MultiRoute(singleRoutes));
            } else {
                route.set(null);
            }
        } else {
            route.set(null);
        }
    }

    /**
     * Returns the index of the segment a given position
     * accounting for empty segments in the route.
     *
     * @param position the position on the route
     * @return the index
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).closestNodeId();
            int n2 = waypoints.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    /**
     * Returns the Property containing the highlighted position on the route
     * (where to place a circle).
     *
     * @return the property
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }

    /**
     * Returns the highlighted position on the route (where to place a circle).
     *
     * @return the highlighted position
     */
    public double highlightedPosition() {
        return highlightedPosition.doubleValue();
    }

    /**
     * Returns a read-only property containing the elevation profile of the route.
     *
     * @return the property
     */
    public ReadOnlyObjectProperty<ElevationProfile> getElevationProfile() {
        return elevationProfile;
    }

    /**
     * Returns the elevation profile of the route.
     *
     * @return the elevation profile
     */
    public ElevationProfile elevationProfile() {
        return elevationProfile.get();
    }

    /**
     * Returns a read-only property containing the route.
     *
     * @return the property
     */
    public ReadOnlyObjectProperty<Route> getRoute() {
        return route;
    }

    /**
     * Returns the route.
     *
     * @return the route
     */
    public Route route() {
        return route.get();
    }

    /**
     * Returns an observable list containing all waypoints in order from start to finish.
     *
     * @return the list of waypoints
     */
    public ObservableList<Waypoint> waypointsObservableList() {
        return waypoints;
    }
}