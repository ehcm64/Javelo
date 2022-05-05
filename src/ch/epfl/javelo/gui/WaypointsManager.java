package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;

import javafx.collections.FXCollections;
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
    private static final String outsideContent =
            "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    private static final String insideContent = "M0-23A1 1 0 000-29 1 1 0 000-23";


    private final Graph graph;
    private ObjectProperty<MapViewParameters> mvp;
    private ObservableList<Waypoint> wayPoints;
    private Consumer<String> error;
    private final Pane pane;
    private List<Group> groups;

    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapViewParameters,
                            ObservableList<Waypoint> wayPoints,
                            Consumer<String> error) {

        this.graph = graph;
        this.mvp = mapViewParameters;
        this.wayPoints = FXCollections.observableArrayList(wayPoints);
        this.error = error;
        this.pane = new Pane();
        this.groups = new ArrayList<>();

        pane.setPickOnBounds(false);
        addHandlers();
        addListeners();
    }

    public Pane pane() {
        return pane;
    }

    private void createGroup(Waypoint w) {
        Group point = new Group();
        SVGPath outside = new SVGPath();
        outside.setContent(outsideContent);
        SVGPath inside = new SVGPath();
        inside.setContent(insideContent);
        outside.getStyleClass().add("pin_outside");
        inside.getStyleClass().add("pin_inside");
        point.getStyleClass().add("pin");
        point.getChildren().add(outside);
        point.getChildren().add(inside);
        groups.add(point);
        setPosition(w, point);
    }

    private void setStyleClass() {
        for (int i = 0; i < groups.size(); i++) {
            Group g = groups.get(i);
            if (g.getStyleClass().size() >= 2)
                g.getStyleClass().remove(g.getStyleClass().size() - 1);
            if (i == 0) {
                g.getStyleClass().add("first");
            } else if (i == groups.size() - 1) {
                g.getStyleClass().add("last");
            } else {
                g.getStyleClass().add("middle");
            }
        }
        addEvents();
    }

    private void addHandlers() {
        wayPoints.forEach(this::createGroup);
        setStyleClass();
    }

    private void addEvents() {

        for (int i = 0; i < groups.size(); i++) {
            Waypoint w = wayPoints.get(i);
            Group g = groups.get(i);

            int finalI = i; // necessary for lambda to work

            g.setOnMouseReleased(e -> {
                if (e.isStillSincePress()) {
                    groups.remove(g);
                    wayPoints.remove(w);
                } else {
                    Waypoint newW = createWaypoint(e.getSceneX(), e.getSceneY());
                    if (newW != null)
                        wayPoints.set(finalI, newW);
                    else {
                        setAllPos();
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
        PointWebMercator pwm = PointWebMercator.ofPointCh(w.position());
        double x = mvp.get().viewX(pwm);
        double y = mvp.get().viewY(pwm);
        point.setLayoutX(x);
        point.setLayoutY(y);
        if (!pane.getChildren().contains(point))
            pane.getChildren().add(point);
    }

    private void setAllPos() {
        for (int i = 0; i < wayPoints.size(); i++) {
            Waypoint w = wayPoints.get(i);
            Group g = groups.get(i);
            setPosition(w, g);
        }
        pane.getChildren().setAll(groups);
    }

    private void addListeners() {
        wayPoints.addListener((ListChangeListener<? super Waypoint>) e -> {

            if (wayPoints.size() > groups.size()) {
                createGroup(wayPoints.get(wayPoints.size() - 1));
            }
            setAllPos();
            setStyleClass();
        });

        mvp.addListener((p, o, n) -> setAllPos());
    }

    private Waypoint createWaypoint(double x, double y) {
        try {
            PointWebMercator pwmFromMousePos = mvp.get().pointAt(x, y);
            PointCh pCh = pwmFromMousePos.toPointCh();
            int closestNode = graph.nodeClosestTo(pCh, SEARCH_DISTANCE);
            if (closestNode == -1)
                throw new IllegalArgumentException();
            return new Waypoint(pCh, closestNode);
        } catch (Exception e) {
            error.accept("Aucune route à proximité !");
        }
        return null;
    }

    public void addWaypoint(double x, double y) {
        Waypoint w = createWaypoint(x, y);
        if (w != null)
            wayPoints.add(createWaypoint(x, y));
    }
}
