package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

import java.util.function.Consumer;

public final class RouteManager {
    private RouteBean rteBean;
    private ReadOnlyObjectProperty<MapViewParameters> mvpProperty;
    private Consumer<String> errorConsumer;

    public RouteManager(RouteBean rteBean,
                        ReadOnlyObjectProperty<MapViewParameters> mvpProperty,
                        Consumer<String> errorConsumer) {

        this.rteBean = rteBean;
        this.mvpProperty = mvpProperty;
        this.errorConsumer = errorConsumer;
    }

    public Pane pane() {
        Pane pane = new Pane();
        Polyline rteLine = new Polyline();
        rteLine.setId("route");

        PointWebMercator point0 = PointWebMercator.ofPointCh(rteBean.routeProperty().get().points().get(0));
        rteLine.getPoints().addAll(point0.x(), point0.y());
        return null;
    }
}
