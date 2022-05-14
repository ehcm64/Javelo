package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
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
    private final ObjectProperty<MapViewParameters> mapViewParameters;
    private boolean redrawNeeded;
    private final Canvas canvas;
    private final Pane pane;
    private final ObjectProperty<Point2D> mouseAnchor;

    private static final int LOWEST_ZOOM_LEVEL = 8;
    private static final int HIGHEST_ZOOM_LEVEL = 19;
    private static final int TILE_LENGTH = 256;

    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapViewParameters) {

        this.tm = tileManager;
        this.wpm = waypointsManager;
        this.mapViewParameters = mapViewParameters;
        this.pane = new Pane();
        this.canvas = new Canvas();
        this.redrawNeeded = true;
        this.mouseAnchor = new SimpleObjectProperty<>();

        pane.getChildren().add(canvas);

        addBinds();
        addListeners();
        addHandlers();
    }

    public Pane pane() {
        return pane;
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        MapViewParameters mvp = mapViewParameters.get();

        int zl = mvp.zoomLevel();
        double xTopLeft = mvp.xTopLeft();
        double yTopLeft = mvp.yTopLeft();

        int xTopLeftTileId = (int) (xTopLeft / TILE_LENGTH);
        int yTopLeftTileId = (int) (yTopLeft / TILE_LENGTH);

        int xBottomRightTileId = (int) ((xTopLeft + pane.getWidth()) / TILE_LENGTH);
        int yBottomRightTileId = (int) ((yTopLeft + pane.getHeight()) / TILE_LENGTH);

        for (int x = xTopLeftTileId; x <= xBottomRightTileId; x++) {
            for (int y = yTopLeftTileId; y <= yBottomRightTileId; y++) {
                TileManager.TileId tileId = new TileManager.TileId(zl, x, y);
                try {
                    gc.drawImage(
                            tm.imageForTileAt(tileId),
                            x * TILE_LENGTH - xTopLeft,
                            y * TILE_LENGTH - yTopLeft);
                } catch (IOException e) {
                    gc.setFill(Color.PAPAYAWHIP);
                    gc.fillRect(
                            x * TILE_LENGTH - xTopLeft,
                            y * TILE_LENGTH - yTopLeft,
                            TILE_LENGTH,
                            TILE_LENGTH);
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

        pane.setOnMousePressed(e -> mouseAnchor.set(
                new Point2D(e.getX(), e.getY())));

        pane.setOnMouseDragged(e -> {

            if (!e.isStillSincePress()) {

                MapViewParameters mvp = mapViewParameters.get();

                Point2D topLeft = mvp.topLeft();
                Point2D eXY = new Point2D(e.getX(), e.getY());
                Point2D newTopLeft = topLeft.add(mouseAnchor.get())
                                            .subtract(eXY);

                mapViewParameters.set(
                        mvp.withMinXY(newTopLeft.getX(),
                                      newTopLeft.getY()));
                mouseAnchor.set(eXY);
            }
        });

        pane.setOnScroll(e -> {

            MapViewParameters mvp = mapViewParameters.get();
            int zoomLevel = mvp.zoomLevel();
            int zoomDiff = (int) Math.round(
                    Math2.clamp(-1, e.getDeltaY(), 1));
            int newZoomLevel = Math2.clamp(
                    LOWEST_ZOOM_LEVEL,
                    zoomLevel + zoomDiff,
                    HIGHEST_ZOOM_LEVEL);

            double newMouseX = Math.scalb(e.getX() + mvp.xTopLeft(),
                    newZoomLevel - zoomLevel);
            double newMouseY = Math.scalb(e.getY() + mvp.yTopLeft(),
                    newZoomLevel - zoomLevel);

            double newXTopLeft = newMouseX - e.getX();
            double newYTopLeft = newMouseY - e.getY();

            mapViewParameters.set(
                    new MapViewParameters(newZoomLevel,
                            newXTopLeft,
                            newYTopLeft));
        });
    }

    private void addListeners() {

        mapViewParameters.addListener((p, o, n) -> redrawOnNextPulse());
        pane.widthProperty().addListener((p, o, n) -> redrawOnNextPulse());
        pane.heightProperty().addListener((p, o, n) -> redrawOnNextPulse());
        mouseAnchor.addListener((p, o, n) -> redrawOnNextPulse());

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
