package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public final class AnnotatedMapManager {

    private final ObjectProperty<Point2D> mousePositionProperty;
    private final DoubleProperty mousePositionOnRouteProperty;
    private final ObjectProperty<MapViewParameters> mvpProperty;
    private final ReadOnlyObjectProperty<Route> routeProperty;
    private final Pane pane;


    private static final int INITIAL_X_TOP_LEFT = 543200;
    private static final int INITIAL_Y_TOP_LEFT = 370650;
    private static final int INITIAL_ZOOM_LEVEL = 12;

    public AnnotatedMapManager(Graph graph,
                               TileManager tileManager,
                               RouteBean routeBean,
                               Consumer<String> errorConsumer) {

        MapViewParameters mapViewParameters = new MapViewParameters(
                INITIAL_ZOOM_LEVEL,
                INITIAL_X_TOP_LEFT,
                INITIAL_Y_TOP_LEFT);

        mvpProperty = new SimpleObjectProperty<>(mapViewParameters);
        mousePositionProperty = new SimpleObjectProperty<>();
        mousePositionOnRouteProperty = new SimpleDoubleProperty();

        WaypointsManager wpm = new WaypointsManager(graph, mvpProperty, routeBean.waypointsObservableList(), errorConsumer);
        BaseMapManager bmp = new BaseMapManager(tileManager, wpm, mvpProperty);
        RouteManager routeManager = new RouteManager(routeBean, mvpProperty, errorConsumer);

        routeProperty = routeBean.getRoute();

        pane = new Pane();

        pane.getChildren().addAll(bmp.pane(), wpm.pane(), routeManager.pane());

        addEvents();
        mousePositionOnRouteProperty.bind(
                Bindings.createDoubleBinding(() -> {
                    MapViewParameters mvp = mvpProperty.get();
                    Route route = routeProperty.get();
                    Point2D mouse = mousePositionProperty.get();
                    PointCh mousePoint = mvp
                            .pointAt(mouse.getX(), mouse.getY())
                            .toPointCh();
                    RoutePoint routePoint = route.pointClosestTo(mousePoint);
                    double pos = routePoint.position();
                    PointCh routePointCh = routePoint.point();
                    PointWebMercator routePwm = PointWebMercator.ofPointCh(routePointCh);
                    Point2D routePoint2D = new Point2D(mvp.viewX(routePwm), mvp.viewY(routePwm));
                    double distance = routePoint2D.distance(mouse);
                    if (distance > 15) return Double.NaN;
                    return pos;
        }, mousePositionProperty));
    }

    public Pane pane() {
        return pane;
    }

    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }

    private void addEvents() {
        pane.setOnMouseMoved(e -> {
            mousePositionProperty.set(new Point2D(e.getX(), e.getY()));
        });

        pane.setOnMouseExited(e -> mousePositionOnRouteProperty.set(Double.NaN));
    }
}