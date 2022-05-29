package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;

public final class BaseMapManager {
    private final TileManager tm;
    private final WaypointsManager wpm;
    private final ObjectProperty<MapViewParameters> mvpProperty;
    private final Canvas canvas;
    private final Pane pane;
    private final ObjectProperty<Point2D> mouseAnchorProperty;
    private final SimpleLongProperty minScrollTime;

    private boolean redrawNeeded;

    private static final int LOWEST_ZOOM_LEVEL = 8;
    private static final int HIGHEST_ZOOM_LEVEL = 19;
    private static final int TILE_LENGTH = 256;

    /**
     * Represents the part of the GUI that displays the map.
     *
     * @param tileManager      the tile manager
     * @param waypointsManager the waypoints manager
     * @param mvpProperty      the property containing the map view parameters
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mvpProperty) {

        this.tm = tileManager;
        this.wpm = waypointsManager;
        this.mvpProperty = mvpProperty;
        pane = new Pane();
        canvas = new Canvas();
        redrawNeeded = true;
        mouseAnchorProperty = new SimpleObjectProperty<>();
        minScrollTime = new SimpleLongProperty();

        pane.getChildren().add(canvas);

        addBinds();
        addListeners();
        addHandlers();
    }

    /**
     * Returns a pane containing the displayed map.
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        MapViewParameters mvp = mvpProperty.get();

        int xTopLeftTileId = (int) (mvp.xTopLeft() / TILE_LENGTH);
        int yTopLeftTileId = (int) (mvp.yTopLeft() / TILE_LENGTH);

        int xBottomRightTileId = (int) ((mvp.xTopLeft() + pane.getWidth()) / TILE_LENGTH);
        int yBottomRightTileId = (int) ((mvp.yTopLeft() + pane.getHeight()) / TILE_LENGTH);

        for (int x = xTopLeftTileId; x <= xBottomRightTileId; x++) {
            for (int y = yTopLeftTileId; y <= yBottomRightTileId; y++) {
                TileManager.TileId tileId = new TileManager.TileId(mvp.zoomLevel(), x, y);
                double xAnchor = x * TILE_LENGTH - mvp.xTopLeft();
                double yAnchor = y * TILE_LENGTH - mvp.yTopLeft();

                try {
                    gc.drawImage(tm.imageForTileAt(tileId), xAnchor, yAnchor);
                } catch (IOException e) {
                    gc.setFill(Color.PAPAYAWHIP);
                    gc.fillRect(xAnchor, yAnchor, TILE_LENGTH, TILE_LENGTH);
                    // We draw a beige rectangle instead of the tile if there is an exception
                }
            }
        }
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void addHandlers() {

        pane.setOnMouseClicked(e -> {
            if (e.isStillSincePress()) {
                wpm.addWaypoint(e.getX(), e.getY());
            }
        });

        pane.setOnMousePressed(e -> mouseAnchorProperty.set(
                new Point2D(e.getX(), e.getY())));

        pane.setOnMouseDragged(e -> {

            if (!e.isStillSincePress()) {
                MapViewParameters mvp = mvpProperty.get();

                Point2D topLeft = mvp.topLeft();
                Point2D oldMousePos = mouseAnchorProperty.get();
                Point2D newMousePos = new Point2D(e.getX(), e.getY());
                Point2D newTopLeft = topLeft.add(oldMousePos).subtract(newMousePos);

                mvpProperty.set(
                        mvp.withMinXY(newTopLeft.getX(),
                                newTopLeft.getY()));
                mouseAnchorProperty.set(newMousePos);
            }
        });

        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math.signum(e.getDeltaY());

            MapViewParameters mvp = mvpProperty.get();
            int newZoomLevel = Math2.clamp(
                    LOWEST_ZOOM_LEVEL,
                    mvp.zoomLevel() + zoomDelta,
                    HIGHEST_ZOOM_LEVEL);

            double newMouseX = Math.scalb(e.getX() + mvp.xTopLeft(),
                    newZoomLevel - mvp.zoomLevel());
            double newMouseY = Math.scalb(e.getY() + mvp.yTopLeft(),
                    newZoomLevel - mvp.zoomLevel());

            double newXTopLeft = newMouseX - e.getX();
            double newYTopLeft = newMouseY - e.getY();

            mvpProperty.set(
                    new MapViewParameters(newZoomLevel,
                            newXTopLeft,
                            newYTopLeft));
        });
    }

    private void addListeners() {

        mvpProperty.addListener(p -> redrawOnNextPulse());
        pane.widthProperty().addListener(p -> redrawOnNextPulse());
        pane.heightProperty().addListener(p -> redrawOnNextPulse());
        mouseAnchorProperty.addListener(p -> redrawOnNextPulse());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    private void addBinds() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }
}
