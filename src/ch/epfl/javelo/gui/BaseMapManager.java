package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;

public final class BaseMapManager {
    private final TileManager tm;
    private final WaypointsManager wpm;
    private ObjectProperty<MapViewParameters> mvp;
    private boolean redrawNeeded;
    private Canvas canvas;
    private Pane pane;
    private ObjectProperty<MouseEvent> mouse;

    public BaseMapManager(TileManager tm,
                          WaypointsManager wpm,
                          ObjectProperty<MapViewParameters> mvp) {

        this.tm = tm;
        this.wpm = wpm;
        this.mvp = mvp;
        this.pane = new Pane();
        this.canvas = new Canvas();
        this.redrawNeeded = true;
        this.mouse = new SimpleObjectProperty<>();

        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        addHandlers();


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

    private void addHandlers() {

        pane.setOnMousePressed(e -> mouse.set(e));

        pane.setOnMouseDragged(e -> {

            mvp.set(mvp.get().withMinXY(mvp.get().xTopLeft() - (e.getX() - mouse.get().getX()),
                    mvp.get().yTopLeft() - (e.getY() - mouse.get().getY())));
            mouse.set(e);
        });
        pane.setOnMouseReleased(e -> mouse.set(e));

        pane.setOnScroll(e -> {
            int zoomLevel = mvp.get().zoomLevel();
            int zoomDiff = (int) Math.round(e.getDeltaY() / 25);
            int newZoomLevel = Math2.clamp(8, mvp.get().zoomLevel() + zoomDiff, 19);

            double newMouseX = Math.scalb(e.getX() + mvp.get().xTopLeft(), -zoomLevel + newZoomLevel);
            double newMouseY = Math.scalb(e.getY() + mvp.get().yTopLeft(), -zoomLevel + newZoomLevel);

            double newXTopLeft = newMouseX - e.getX();
            double newYTopLeft = newMouseY - e.getY();

            mvp.set(new MapViewParameters(newZoomLevel,
                    newXTopLeft,
                    newYTopLeft));
        });
    }
}
