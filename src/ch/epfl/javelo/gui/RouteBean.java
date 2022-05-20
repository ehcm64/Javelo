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
 * Groups the properties relating to the crossing points and the corresponding route
 * @author Edouard Mignan (345875)
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
    private static final double MOUSE_NOT_ON_ROAD = Double.NaN;

    /**
     * Constructs a a group which contains properties relating
     * to the crossing points and the corresponding route
     * @param routeComputer used to determine the best route between two transit points.
     */
    public RouteBean(RouteComputer routeComputer) {

        this.elevationProfile = new SimpleObjectProperty<>();
        this.route = new SimpleObjectProperty<>();
        this.routeComputer = routeComputer;
        this.highlightedPosition = new SimpleDoubleProperty(MOUSE_NOT_ON_ROAD);

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
     * Returns the index of the segment containing it, ignoring empty segments
     * @param position a position along the itinerary
     * @return the index of the segment containing it, ignoring empty segments
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

    private Route route() {
        return route.get();
    }
    /**
     * Returns the highlighted position on the route
     * @return the highlighted position on the route
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }

    /**
     * Returns the highlighted position on the route
     * @return the double value of the highlighted position on the map
     */

    public double highlightedPosition() {
        return highlightedPosition.doubleValue();
    }

    /**
     * Returns the profile of the elevation which is read-only
     * @return profile of the elevation (ReadOnlyObjectProperty)
     */
    public ReadOnlyObjectProperty<ElevationProfile> getElevationProfile() {
        return elevationProfile;
    }

    /**
     * Returns the itinerary which is read-only
     * @return itinerary (ReadOnlyObjectProperty)
     */
    public ReadOnlyObjectProperty<Route> getRoute() {
        return route;
    }
    /**
     * Returns the list of all the waypoints
     * @return list of waypoints (ObservableList)
     */
    public ObservableList<Waypoint> waypointsObservableList() {
        return waypoints;
    }
}