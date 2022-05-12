package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import java.util.function.Consumer;

public final class RouteManager {
    private Pane pane;
    private RouteBean routeBean;
    private ReadOnlyProperty<MapViewParameters> mapViewParameters;
    private Consumer<String> errorConsumer;
    private Polyline routeLine;
    private Circle positionCircle;

    public RouteManager(RouteBean routeBean,
                        ReadOnlyProperty<MapViewParameters> mapViewParameters,
                        Consumer<String> errorConsumer) {

        this.routeBean = routeBean;
        this.mapViewParameters = mapViewParameters;
        this.errorConsumer = errorConsumer;

        pane = new Pane();
        pane.setPickOnBounds(false);

        addListeners();

        routeLine = new Polyline();
        routeLine.setId("route");

        positionCircle = new Circle(5);
        positionCircle.setVisible(false);
        positionCircle.setId("highlight");

        pane.getChildren().add(routeLine);
        pane.getChildren().add(positionCircle);

    }

    public Pane pane() {
        return pane;
    }

    private void addListeners() {

        routeBean.highlightedPositionProperty().addListener((p, o, n) -> {
            if (n == null) {
                positionCircle.setVisible(false);
                replaceRouteAndCircle();
            } else {
                positionCircleAt(n.doubleValue());
                replaceRouteAndCircle();
            }
        });
        routeBean.getRoute().addListener((p, o, n) -> {
            if (n == null) {
                routeLine.setVisible(false);
                positionCircle.setVisible(false);
                replaceRouteAndCircle();
            } else {
                calculateRouteLine();
                positionCircleAt(routeBean.highlightedPosition());
                routeLine.setVisible(true);
                positionCircle.setVisible(true);
                replaceRouteAndCircle();
            }
        });

        mapViewParameters.addListener((p, o, n) -> {
            if (o.zoomLevel() != n.zoomLevel()) {
                calculateRouteLine();
                positionCircleAt(routeBean.highlightedPosition());
                replaceRouteAndCircle();
            } else if (o.xTopLeft() != n.xTopLeft() || o.yTopLeft() != n.yTopLeft()) {
                double oldX = routeLine.getLayoutX();
                double oldY = routeLine.getLayoutY();
                routeLine.setLayoutX(oldX + o.xTopLeft() - n.xTopLeft());
                routeLine.setLayoutY(oldY + o.yTopLeft() - n.yTopLeft());
                positionCircle.setLayoutX(oldX + o.xTopLeft() - n.xTopLeft());
                positionCircle.setLayoutY(oldY + o.yTopLeft() - n.yTopLeft());
                replaceRouteAndCircle();
            }
        });
    }

    private void calculateRouteLine() {
        Polyline line = new Polyline();
        if (routeBean.getRoute().get() == null) {
            routeLine = line;
            routeLine.setVisible(false);
            return;
        } else {
            MapViewParameters mvp = mapViewParameters.getValue();
            for (PointCh point : routeBean.getRoute().get().points()) {
                PointWebMercator pwm = PointWebMercator.ofPointCh(point);
                double x = pwm.xAtZoomLevel(mvp.zoomLevel());
                double y = pwm.yAtZoomLevel(mvp.zoomLevel());

                line.getPoints().add(x - mvp.xTopLeft());
                line.getPoints().add(y - mvp.yTopLeft());
            }
        }
        routeLine = line;
    }

    private void positionCircleAt(double position) {
        MapViewParameters mvp = mapViewParameters.getValue();
        PointCh point = routeBean.getRoute().get().pointAt(position);
        PointWebMercator pwm = PointWebMercator.ofPointCh(point);
        double x = pwm.xAtZoomLevel(mvp.zoomLevel());
        double y = pwm.yAtZoomLevel(mvp.zoomLevel());
        positionCircle.setCenterX(x - mvp.xTopLeft());
        positionCircle.setCenterY(y - mvp.yTopLeft());
    }

    private void replaceRouteAndCircle() {
        pane.getChildren().clear();
        pane.getChildren().add(routeLine);
        pane.getChildren().add(positionCircle);
    }
}
