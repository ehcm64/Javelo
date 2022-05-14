package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;

import java.util.List;
import java.util.function.Consumer;

public final class WaypointsManager {

    private static final double SEARCH_DISTANCE = 500;
    private static final String OUTSIDE_CONTENT_SVG =
            "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    private static final String INSIDE_CONTENT_SVG =
            "M0-23A1 1 0 000-29 1 1 0 000-23";
    private static final String OUTSIDE_PIN_CSS = "pin_outside";
    private static final String INSIDE_PIN_CSS = "pin_inside";
    private static final String PIN_CSS = "pin";
    private static final String FIRST_WAYPOINT_CSS = "first";
    private static final String MIDDLE_WAYPOINT_CSS = "middle";
    private static final String LAST_WAYPOINT_CSS = "last";
    private static final String ERROR_STRING = "Aucune route à proximité !";


    private final Graph graph;
    private final ObjectProperty<MapViewParameters> mapViewParameters;
    private final ObservableList<Waypoint> wayPoints;
    private final Consumer<String> error;
    private final Pane pane;
    private final List<Group> groups;

    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapViewParameters,
                            ObservableList<Waypoint> wayPoints,
                            Consumer<String> error) {

        this.graph = graph;
        this.mapViewParameters = mapViewParameters;
        this.wayPoints = wayPoints;
        this.error = error;
        this.pane = new Pane();
        this.groups = new ArrayList<>();

        pane.setPickOnBounds(false);
        addListeners();
    }

    public Pane pane() {
        return pane;
    }

    public void addWaypoint(double x, double y) {

        if (waypointCanExist(x, y)) {
            Waypoint w = waypointFromXAndY(x, y);
            wayPoints.add(w);
        } else {
            error.accept(ERROR_STRING);
        }
    }

    private void addHandlers() {
        for (Group g : groups) {
            int indexOfg = groups.indexOf(g);

            g.setOnMouseDragged(e -> {
                if (!e.isStillSincePress()) {
                    g.setLayoutX(e.getSceneX());
                    g.setLayoutY(e.getSceneY());
                }
            });

            g.setOnMouseReleased(e -> {
                if (e.isStillSincePress()) {
                    wayPoints.remove(indexOfg);
                } else {
                    if (waypointCanExist(e.getSceneX(), e.getSceneY())) {
                        wayPoints.set(indexOfg,
                                waypointFromXAndY(e.getSceneX(), e.getSceneY()));
                    } else {
                        Waypoint w = wayPoints.get(indexOfg);
                        positionGroup(g, w);
                        error.accept(ERROR_STRING);
                    }
                }
            });
        }
    }

    private void addListeners() {
        mapViewParameters.addListener((p, o, n) -> {
            for (Group g : groups) {
                int indexOfg = groups.indexOf(g);
                Waypoint w = wayPoints.get(indexOfg);
                positionGroup(g, w);
            }
        });

        wayPoints.addListener((ListChangeListener<? super Waypoint>) e -> {
            groups.clear();
            pane.getChildren().clear();
            for (Waypoint w : wayPoints) {
                Group g = createGroup(w);
                groups.add(g);
                pane.getChildren().add(g);
            }
            addHandlers();
        });
    }

    private Group createGroup(Waypoint w) {

        SVGPath outside = new SVGPath();
        outside.setContent(OUTSIDE_CONTENT_SVG);
        outside.getStyleClass().add(OUTSIDE_PIN_CSS);
        SVGPath inside = new SVGPath();
        inside.setContent(INSIDE_CONTENT_SVG);
        inside.getStyleClass().add(INSIDE_PIN_CSS);

        Group g = new Group();
        g.getChildren().add(outside);
        g.getChildren().add(inside);
        g.getStyleClass().add(PIN_CSS);

        int indexOfw = wayPoints.indexOf(w);

        if (indexOfw == 0) {
            g.getStyleClass().add(FIRST_WAYPOINT_CSS);
        } else if (indexOfw == wayPoints.size() - 1) {
            g.getStyleClass().add(LAST_WAYPOINT_CSS);
        } else {
            g.getStyleClass().add(MIDDLE_WAYPOINT_CSS);
        }
        positionGroup(g, w);
        return g;
    }

    private boolean waypointCanExist(double x, double y) {
        MapViewParameters mvp = mapViewParameters.get();
        PointCh point = mvp.pointAt(x, y).toPointCh();
        int nodeId = -1;
        if (point != null)
            nodeId = graph.nodeClosestTo(point, SEARCH_DISTANCE);

        return nodeId != -1;
    }

    private Waypoint waypointFromXAndY(double x, double y) {
        MapViewParameters mvp = mapViewParameters.get();
        PointCh point = mvp.pointAt(x, y).toPointCh();
        int nodeId = graph.nodeClosestTo(point, SEARCH_DISTANCE);
        return new Waypoint(point, nodeId);
    }

    private void positionGroup(Group g, Waypoint w) {
        MapViewParameters mvp = mapViewParameters.get();
        PointWebMercator pwm = PointWebMercator.ofPointCh(w.position());
        double x = pwm.xAtZoomLevel(mvp.zoomLevel()) - mvp.xTopLeft();
        double y = pwm.yAtZoomLevel(mvp.zoomLevel()) - mvp.yTopLeft();

        g.setLayoutX(x);
        g.setLayoutY(y);
    }
}
