package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ListChangeListener;
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

        routeLine = new Polyline();
        routeLine.setId("route");

        positionCircle = new Circle(5);
        positionCircle.setId("highlight");

        pane.getChildren().add(routeLine);
        pane.getChildren().add(positionCircle);

    }

    public Pane pane() {
        return pane;
    }

    private void addListeners() {
        routeBean.waypointsObservableList().addListener((ListChangeListener<? super Waypoint>) e -> {
            routeLine = getPolyline();
        });

        routeBean.getRoute().addListener((p, o, n) -> {
            if (routeBean.getRoute() == null)  {
                routeLine.setVisible(false);
                positionCircle.setVisible(false);
            }



        });
    }

    private Polyline getPolyline() {
        return null;
    }
}
