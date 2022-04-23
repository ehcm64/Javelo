package ch.epfl.javelo.gui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;

public final class BaseMapManager {
    private TileManager tm;
    private WaypointsManager wpm;
    private ObjectProperty<MapViewParameters> mvp;
    private boolean redrawNeeded;
    private Canvas canvas;
    private Pane pane;

    public BaseMapManager(TileManager tm,
                          WaypointsManager wpm,
                          ObjectProperty<MapViewParameters> mvp) {

        this.tm = tm;
        this.wpm = wpm;
        this.mvp = mvp;
        this.pane = new Pane();
        this.canvas = new Canvas();
        this.redrawNeeded = true;

        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    public Pane pane() {
        return pane;
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        int zl = mvp.get().zoomLevel();
        double xTopLeft = mvp.get().xTopLeft();
        double yTopLeft = mvp.get().yTopLeft();

        int xTopLeftTileId = (int) Math.floor(xTopLeft / 256);
        int yTopLeftTileId = (int) Math.floor(yTopLeft / 256);

        int xBottomRightTileId = (int) Math.floor((xTopLeft + canvas.getWidth()) / 256);
        int yBottomRightTileId = (int) Math.floor((yTopLeft + canvas.getHeight()) / 256);

        for (int x = xTopLeftTileId; x <= xBottomRightTileId; x++) {
            for (int y = yTopLeftTileId; y <= yBottomRightTileId; y++) {
                TileManager.TileId tileId = new TileManager.TileId(zl, x, y);

                try {
                    gc.drawImage(tm.imageForTileAt(tileId),
                            x * 256 - xTopLeft,
                            y * 256 - yTopLeft);
                } catch (IOException e) {
                    // We don't draw image if there is an exception
                }
            }
        }
        redrawOnNextPulse();
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}
