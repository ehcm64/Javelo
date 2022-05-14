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


public final class RouteBean {

    private ObservableList<Waypoint> waypoints;
    private ObjectProperty<Route> route;
    private ObjectProperty<ElevationProfile> elevationProfile;
    private DoubleProperty highlightedPosition;
    private RouteComputer routeComputer;
    private Map<Pair<Integer, Integer>, Route> memoryCache;

    private static final int MAX_STEP_LENGTH = 5;
    private static final int CACHE_SIZE = 50;

    public RouteBean(RouteComputer routeComputer) {

        this.elevationProfile = new SimpleObjectProperty<>();
        this.route = new SimpleObjectProperty<>();
        this.routeComputer = routeComputer;

        waypoints = FXCollections.observableArrayList();

        memoryCache = new LinkedHashMap<>(CACHE_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };

        waypoints.addListener((ListChangeListener<? super Waypoint>) e -> buildRoute());

        route.addListener((p, o, n) -> buildElevationProfile());
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
                Pair<Integer, Integer> pair = new Pair<>(startNodeId, endNodeId);
                boolean needToCalculateRoute = true;

                for (Pair p : memoryCache.keySet()) {
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
            route.set(new MultiRoute(singleRoutes));
        } else {
            route.set(null);
        }
    }

    public void setHighlightedPosition(DoubleProperty newHighlightedPosition) {
        highlightedPosition = newHighlightedPosition;
    }

    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }

    public double highlightedPosition() {
        return highlightedPosition.doubleValue();
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = FXCollections.observableArrayList(waypoints);
    }

    public ObservableList<Waypoint> waypointsObservableList() {
        return waypoints;
    }

    public ReadOnlyObjectProperty<ElevationProfile> getElevationProfile() {
        return elevationProfile;
    }

    public ReadOnlyObjectProperty<Route> getRoute() {
        return route;
    }
}