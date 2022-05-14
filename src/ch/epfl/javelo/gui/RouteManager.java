package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import java.util.function.Consumer;

public final class RouteManager {
    private final Pane pane;
    private final RouteBean routeBean;
    private final ReadOnlyProperty<MapViewParameters> mapViewParameters;
    private final Consumer<String> errorConsumer;
    private final Polyline routeLine;
    private final Circle positionCircle;

    public RouteManager(RouteBean routeBean,
                        ReadOnlyProperty<MapViewParameters> mapViewParameters,
                        Consumer<String> errorConsumer) {

        this.routeBean = routeBean;
        this.mapViewParameters = mapViewParameters;
        this.errorConsumer = errorConsumer;

        pane = new Pane();
        pane.setPickOnBounds(false);

        routeLine = new Polyline();
        routeLine.setId("route");

        positionCircle = new Circle(5);
        positionCircle.setVisible(false);
        positionCircle.setId("highlight");

        pane.getChildren().add(routeLine);
        pane.getChildren().add(positionCircle);

        addListeners();
        addEvents();
    }

    public Pane pane() {
        return pane;
    }

    private void addListeners() {

        routeBean.highlightedPositionProperty().addListener((p, o, n) -> {
            setRouteLine();
            setCircle();
        });

        routeBean.getRoute().addListener((p, o, n) -> {
            setRouteLine();
            setCircle();
        });

        mapViewParameters.addListener((p, o, n) -> {
            if (o.zoomLevel() != n.zoomLevel()) {
                setRouteLine();
                setCircle();
            } else if (o.xTopLeft() != n.xTopLeft() || o.yTopLeft() != n.yTopLeft()) {
                routeLine.setLayoutX(
                        routeLine.getLayoutX() + o.xTopLeft() - n.xTopLeft());
                routeLine.setLayoutY(
                        routeLine.getLayoutY() + o.yTopLeft() - n.yTopLeft());
                positionCircle.setLayoutX(
                        positionCircle.getLayoutX() + o.xTopLeft() - n.xTopLeft());
                positionCircle.setLayoutY(
                        positionCircle.getLayoutY() + o.yTopLeft() - n.yTopLeft());
            }
        });
    }

    private void addEvents() {

        positionCircle.setOnMouseClicked(e -> {
            Route route = routeBean.getRoute().get();
            double hPosition = routeBean.highlightedPosition();
            MapViewParameters mvp = mapViewParameters.getValue();
            Point2D mouse = positionCircle.localToParent(e.getX(), e.getY());
            double x = mvp.xTopLeft() + mouse.getX();
            double y = mvp.yTopLeft() + mouse.getY();

            PointCh point = mvp.pointAt(x, y).toPointCh();
            int circleNode = route.nodeClosestTo(hPosition);

            for (int i = 1; i < routeBean.waypointsObservableList().size(); i++) {
                Waypoint w = routeBean.waypointsObservableList().get(i);
                if (circleNode == w.closestNodeId()) {
                    errorConsumer.accept("Un point de passage est déja présent à cet endroit !");
                    break;
                } else if (route.pointClosestTo(w.position()).position() > hPosition) {
                    Waypoint circleWaypoint = new Waypoint(point, circleNode);
                    routeBean.waypointsObservableList().add(i, circleWaypoint);
                    break;
                }
            }
        });
    }

    private void setRouteLine() {
        Route route = routeBean.getRoute().get();
        if (route == null) {
            routeLine.setVisible(false);
            return;
        }
        MapViewParameters mvp = mapViewParameters.getValue();
        routeLine.getPoints().clear();
        for (PointCh point : route.points()) {
            PointWebMercator pwm = PointWebMercator.ofPointCh(point);
            routeLine.getPoints().add(mvp.viewX(pwm));
            routeLine.getPoints().add(mvp.viewY(pwm));
        }
        routeLine.setVisible(true);
    }

    private void setCircle() {
        Route route = routeBean.getRoute().get();
        if (route == null) {
            positionCircle.setVisible(false);
            return;
        }
        MapViewParameters mvp = mapViewParameters.getValue();
        double hPosition = routeBean.highlightedPosition();
        PointCh point = route.pointAt(hPosition);
        PointWebMercator pwm = PointWebMercator.ofPointCh(point);
        positionCircle.setLayoutX(mvp.viewX(pwm));
        positionCircle.setLayoutY(mvp.viewY(pwm));

        positionCircle.setVisible(true);
    }
}