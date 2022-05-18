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

    private static final String ROUTE_ID = "route";
    private static final String HIGHLIGHT_ID = "highlight";
    private static final String WAYPOINT_ALREADY_EXISTS_WARNING =
            "Un point de passage est déja présent à cet endroit !";

    public RouteManager(RouteBean routeBean,
                        ReadOnlyProperty<MapViewParameters> mapViewParameters,
                        Consumer<String> errorConsumer) {

        this.routeBean = routeBean;
        this.mapViewParameters = mapViewParameters;
        this.errorConsumer = errorConsumer;

        pane = new Pane();
        pane.setPickOnBounds(false);

        routeLine = new Polyline();
        routeLine.setId(ROUTE_ID);

        positionCircle = new Circle(5);
        positionCircle.setVisible(false);
        positionCircle.setId(HIGHLIGHT_ID);

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

            PointCh point = mvp.pointAt(mouse.getX(), mouse.getY()).toPointCh();
            int circleNode = route.nodeClosestTo(hPosition);
            Waypoint circleWaypoint = new Waypoint(point, circleNode);
            int waypointIndex = route.indexOfSegmentAt(hPosition) + 1;
            routeBean.waypointsObservableList().add(waypointIndex, circleWaypoint);
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
        routeLine.setLayoutX(0);
        routeLine.setLayoutY(0);
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
