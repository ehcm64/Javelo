package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

/**
 * Represents the part of the GUI that handles displaying the itinerary.
 *
 * @author Edouard Mignan (345875)
 */
public final class RouteManager {
    private final Pane pane;
    private final RouteBean routeBean;
    private final ReadOnlyProperty<MapViewParameters> mapViewParameters;
    private final Polyline routeLine;
    private final Circle positionCircle;

    private static final String ROUTE_ID = "route";
    private static final String HIGHLIGHT_ID = "highlight";
    private static final int POSITION_CIRCLE_RADIUS = 5;

    /**
     * Creates a route manager.
     *
     * @param routeBean         a Java Bean containing properties about the route
     * @param mapViewParameters the property containing the parameters of the map view
     */
    public RouteManager(RouteBean routeBean,
                        ReadOnlyProperty<MapViewParameters> mapViewParameters) {

        this.routeBean = routeBean;
        this.mapViewParameters = mapViewParameters;

        pane = new Pane();
        pane.setPickOnBounds(false);

        routeLine = new Polyline();
        routeLine.setId(ROUTE_ID);

        positionCircle = new Circle(POSITION_CIRCLE_RADIUS);
        positionCircle.setVisible(false);
        positionCircle.setId(HIGHLIGHT_ID);

        pane.getChildren().add(routeLine);
        pane.getChildren().add(positionCircle);

        addListeners();
        addEvents();
    }

    /**
     * Returns a pane displaying the route (if there is one).
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    private void addListeners() {

        routeBean.highlightedPositionProperty().addListener(p -> setCircle());

        routeBean.getRoute().addListener(p -> {
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
            Route route = routeBean.route();
            double hPosition = routeBean.highlightedPosition();
            MapViewParameters mvp = mapViewParameters.getValue();
            Point2D mouse = positionCircle.localToParent(e.getX(), e.getY());

            PointCh point = mvp.pointAt(mouse.getX(), mouse.getY()).toPointCh();
            int circleNode = route.nodeClosestTo(hPosition);
            Waypoint circleWaypoint = new Waypoint(point, circleNode);
            int waypointIndex = routeBean.indexOfNonEmptySegmentAt(hPosition) + 1;
            routeBean.waypointsObservableList().add(waypointIndex, circleWaypoint);
        });
    }

    private void setRouteLine() {
        Route route = routeBean.route();
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
        Route route = routeBean.route();
        if (route == null) {
            positionCircle.setVisible(false);
            return;
        }
        double hPosition = routeBean.highlightedPosition();
        if (Double.isNaN(hPosition)) {
            positionCircle.setVisible(false);
            return;
        }
        MapViewParameters mvp = mapViewParameters.getValue();
        PointCh point = route.pointAt(hPosition);
        PointWebMercator pwm = PointWebMercator.ofPointCh(point);
        positionCircle.setLayoutX(mvp.viewX(pwm));
        positionCircle.setLayoutY(mvp.viewY(pwm));

        positionCircle.setVisible(true);
    }
}
