package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.javelo.projection.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;


import javax.imageio.event.IIOWriteWarningListener;
import java.util.ArrayList;

import java.util.List;
import java.util.function.Consumer;

public final class WaypointsManager {
    private static final double SEARCH_DISTANCE = 500;

    private final Graph graph;
    private ObjectProperty<MapViewParameters> mvp;
    private ObservableList<Waypoint> wayPoints;
    private List<SVGPath> allSVGPath = new ArrayList<>();
    private Consumer<String> error;
    private final Pane pane;
    private ObjectProperty<Point2D> mouseAnchor;
    private List<Group> groups = new ArrayList<>();

    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapViewParameters,
                            ObservableList<Waypoint> wayPoints,
                            Consumer<String> error) {

        this.graph = graph;
        this.mvp = mapViewParameters;
        this.wayPoints = FXCollections.observableArrayList(wayPoints);
        this.error = error;
        this.pane = new Pane();

        this.mouseAnchor = new SimpleObjectProperty<>();
        pane.setPickOnBounds(false);
        addHandlers();
        addListeners();
        addEvents();
    }

    public Pane pane() {
        return pane;
    }


    private void addHandlers() {
        for (Group g : groups)
            pane.getChildren().remove(g);
        groups.clear();
        for (int i = 0; i < wayPoints.size(); i++) {
            Waypoint w = wayPoints.get(i);
            Group point = new Group();
            SVGPath outside = new SVGPath();
            outside.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
            SVGPath inside = new SVGPath();
            inside.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
            outside.getStyleClass().add("pin_outside");
            inside.getStyleClass().add("pin_inside");
            point.getStyleClass().add("pin");
            if (i == 0) {
                point.getStyleClass().add("first");
            } else if (i == wayPoints.size() - 1) {
                point.getStyleClass().add("last");
            } else {
                point.getStyleClass().add("middle");
            }
            point.getChildren().add(outside);
            point.getChildren().add(inside);
            groups.add(point);
            setPosition(w, point);
        }

    }

    private void addEvents() {
        for (int i = 0; i < groups.size(); i++) {
            Waypoint w = wayPoints.get(i);
            Group g = groups.get(i);
            g.setOnMouseReleased(e -> {
                if (e.isStillSincePress()) {
                    wayPoints.remove(w);
                } else {
                    PointWebMercator pwm = PointWebMercator.of(
                            mvp.get().zoomLevel(),
                            mvp.get().xTopLeft() + e.getSceneX(),
                            mvp.get().yTopLeft() + e.getSceneY());
                    PointCh pCh = pwm.toPointCh();
                    if (graph.nodeClosestTo(pCh, SEARCH_DISTANCE) != -1) {
                        g.setLayoutX(e.getSceneX());
                        g.setLayoutY(e.getSceneY());
                    } else {
                        error.accept("Aucune route à proximité");
                        setPosition(w, g);
                    }
                }
            });
            g.setOnMouseDragged(e -> {
                g.setLayoutX(e.getSceneX());
                g.setLayoutY(e.getSceneY());
            });
        }

    }

    private void setPosition(Waypoint w, Group point) {

        PointCh pCh = w.position();

        if (graph.nodeClosestTo(pCh, SEARCH_DISTANCE) != -1) {
            PointWebMercator pwm = PointWebMercator.ofPointCh(pCh);
            double x = mvp.get().viewX(pwm);
            double y = mvp.get().viewY(pwm);

            point.setLayoutX(x);
            point.setLayoutY(y);
            pane.getChildren().add(point);
        }
    }

    private void setAllPos() {
        System.out.println(wayPoints.size() + " " + groups.size());
        for (int i = 0; i < wayPoints.size(); i++) {
            Waypoint w = wayPoints.get(i);
            Group g = groups.get(i);
            setPosition(w, g);
        }
    }

    private void addListeners() {
        wayPoints.addListener((ListChangeListener<? super Waypoint>) e -> {
            addHandlers();
            addEvents();
        });
        mvp.addListener((p, o, n) -> addHandlers());
    }


    public void addWaypoint(double x, double y) {
        Waypoint w = createWaypoint(x, y);
        if (w != null)
            wayPoints.add(w);
    }

    private Waypoint createWaypoint(double x, double y) {
        PointWebMercator pwmFromMousePos = mvp.get().pointAt(x, y);
        PointCh pCh = pwmFromMousePos.toPointCh();
        int closestNode = graph.nodeClosestTo(pCh, SEARCH_DISTANCE);
        if (closestNode == -1) {
            error.accept("Aucune route à proximité !");
            return null;
        }
        return new Waypoint(pCh, closestNode);
    }
}
