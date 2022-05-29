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
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * Represents the part of the GUI that displays the map and all its elements
 * (waypoints, route, highlighted position, displayed errors).
 *
 * @author Edouard Mignan (345875)
 */
public final class AnnotatedMapManager {

    private final ObjectProperty<Point2D> mousePositionProperty;
    private final DoubleProperty mousePositionOnRouteProperty;
    private final ObjectProperty<MapViewParameters> mvpProperty;
    private final RouteBean routeBean;
    private final Pane pane;

    private static final int INITIAL_X_TOP_LEFT = 543_200;
    private static final int INITIAL_Y_TOP_LEFT = 370_650;
    private static final int INITIAL_ZOOM_LEVEL = 12;
    private static final int MAX_MOUSE_TO_ROAD_DISTANCE = 15;

    /**
     * Creates an annotated map manager.
     *
     * @param graph         the graph
     * @param tileManager   the tile manager
     * @param routeBean     the route bean
     * @param errorConsumer the error consumer
     */
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
        mousePositionOnRouteProperty = new SimpleDoubleProperty(Double.NaN);

        WaypointsManager wpm = new WaypointsManager(
                graph,
                mvpProperty,
                routeBean.waypointsObservableList(),
                errorConsumer);

        this.routeBean = routeBean;

        BaseMapManager bmp = new BaseMapManager(tileManager, wpm, mvpProperty);
        RouteManager routeManager = new RouteManager(routeBean,
                mvpProperty);


        pane = new StackPane(bmp.pane(), wpm.pane(), routeManager.pane());
        pane.getStylesheets().add("map.css");

        addEvents();
        addBinds();
    }

    /**
     * Returns a pane displaying the map and all its elements.
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Returns a read-only property containing the position (in meters) of the
     * position highlighted by the mouse on the route.
     *
     * @return the property
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }

    private void addEvents() {
        pane.setOnMouseMoved(e ->
                mousePositionProperty.set(new Point2D(e.getX(), e.getY())));

        pane.setOnMouseExited(e -> mousePositionProperty.set(null));
    }

    private void addBinds() {
        mousePositionOnRouteProperty.bind(
                Bindings.createDoubleBinding(() -> {

                    Route route = routeBean.route();
                    Point2D mouse = mousePositionProperty.get();
                    if (mouse == null) return Double.NaN;
                    MapViewParameters mvp = mvpProperty.get();
                    PointCh mousePoint = mvp
                            .pointAt(mouse.getX(), mouse.getY())
                            .toPointCh();
                    if (route == null || mousePoint == null) return Double.NaN;

                    RoutePoint routePoint = route.pointClosestTo(mousePoint);
                    PointWebMercator pwm = PointWebMercator.ofPointCh(routePoint.point());
                    Point2D routePoint2D = new Point2D(mvp.viewX(pwm), mvp.viewY(pwm));

                    double distance = routePoint2D.distance(mouse);

                    if (distance > MAX_MOUSE_TO_ROAD_DISTANCE) return Double.NaN;
                    else return routePoint.position();
                }, mousePositionProperty, routeBean.getRoute(), mvpProperty));
    }
}
