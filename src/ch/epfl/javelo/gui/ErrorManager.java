package ch.epfl.javelo.gui;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class ErrorManager {
    private final Pane pane;
    private final Transition errorAnimation;

    public ErrorManager() {
        pane = new VBox();
        pane.getStylesheets().add("error.css");
        pane.setMouseTransparent(true);

        FadeTransition firstTransition = new FadeTransition(new Duration(200), pane);
        firstTransition.setFromValue(0);
        firstTransition.setToValue(0.8);

        PauseTransition pauseTransition = new PauseTransition(new Duration(2_000));

        FadeTransition lastTransition = new FadeTransition(new Duration(500), pane);
        lastTransition.setFromValue(0.8);
        lastTransition.setToValue(0);

        errorAnimation = new SequentialTransition(
                firstTransition,
                pauseTransition,
                lastTransition);
    }

    public Pane pane() {
        return pane;
    }

    public void displayError(String errorMessage) {
        if (errorAnimation.getStatus() == Animation.Status.RUNNING)
            errorAnimation.stop();
        pane.getChildren().clear();
        pane.getChildren().add(new Text(errorMessage));
        java.awt.Toolkit.getDefaultToolkit().beep();
        errorAnimation.play();
    }
}
