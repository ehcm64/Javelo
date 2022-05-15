package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public final class ElevationProfileManager {
    private final ReadOnlyObjectProperty<ElevationProfile> profileProperty;
    private final ReadOnlyDoubleProperty positionProperty;
    private final DoubleProperty mousePositionProperty;
    private final ObjectProperty<Rectangle2D> profileRectangleProperty;
    private final ObjectProperty<Transform> screenToWorldProperty;
    private final ObjectProperty<Transform> worldToScreenProperty;

    private final BorderPane borderPane;
    private final Pane pane;
    private final VBox vBox;
    private final Path path;
    private final Group group;
    private final Polygon polygon;
    private final Line line;
    private final Text text;

    private final Insets insets = new Insets(10, 10, 20, 40);


    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profileProperty,
                                   ReadOnlyDoubleProperty positionProperty) throws NonInvertibleTransformException {

        this.profileProperty = profileProperty;
        this.positionProperty = positionProperty;
        mousePositionProperty = new SimpleDoubleProperty();

        screenToWorldProperty = new SimpleObjectProperty<>();
        worldToScreenProperty = new SimpleObjectProperty<>();


        borderPane = new BorderPane();
        pane = new Pane();
        vBox = new VBox();
        path = new Path();
        path.setId("grid");
        group = new Group();
        Text textFirst = new Text("gjgj");
        textFirst.getStyleClass().addAll("grid_label", "horizontal");
        Text textLast = new Text("gdfknjknwkj");
        textLast.getStyleClass().addAll("grid_label", "vertical");
        group.getChildren().addAll(textFirst, textLast);
        //TODO Texts of Group
        polygon = new Polygon();
        polygon.setId("profile");
        line = new Line();
        pane.getChildren().addAll(path, group, polygon, line);
        text = new Text("jkjksjf");
        vBox.getChildren().add(text);
        vBox.setId("profile_data");
        borderPane.setCenter(pane);
        borderPane.setBottom(vBox);
        borderPane.getStylesheets().add("elevation_profile.css");

        profileRectangleProperty = new SimpleObjectProperty<>();
        profileRectangleProperty.set(new Rectangle2D(10, 10 , 0, 0));
        addListeners();

        Affine screenToWorld = new Affine();
        ElevationProfile ep = profileProperty.get();
        Rectangle2D rect = profileRectangleProperty.get();
        screenToWorld.prependTranslation(-rect.getMinX(), -rect.getMinY());
        screenToWorld.prependScale(ep.length() / rect.getWidth(), ep.maxElevation() - ep.minElevation() / rect.getHeight());
        screenToWorld.prependTranslation(0, ep.minElevation());
        screenToWorldProperty.set(screenToWorld);
        worldToScreenProperty.set(screenToWorld.createInverse());

        graphProfile();

    }

    public Pane pane() {
        return borderPane;
    }

    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionProperty;
    }

    private void addListeners() {
        pane.widthProperty().addListener((p, o, n) -> {
            resizeRectangle();
            graphProfile();
        });

        pane.heightProperty().addListener((p, o, n) -> {
            resizeRectangle();
            graphProfile();
        });

    }

    private void graphProfile() {
        Rectangle2D rect = profileRectangleProperty.get();
        ElevationProfile ep = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();
        double step = ep.length() / (rect.getMaxX() - rect.getMinX());
        polygon.getPoints().clear();
        polygon.getPoints().addAll(rect.getMinX(), rect.getMaxY());
        for (double i = 0; i < ep.length(); i += step) {
            Point2D point = worldToScreen.transform(new Point2D(i, ep.elevationAt(i)));
            polygon.getPoints().addAll(point.getX(), point.getY());
        }
        polygon.getPoints().addAll(rect.getMaxX(), rect.getMaxY());
    }

    private void resizeRectangle() {
        double width = Math2.clamp(
                0,
                pane.getWidth() - insets.getRight() - insets.getLeft(),
                pane.getWidth());
        double height = Math2.clamp(
                0,
                pane.getHeight() - insets.getTop() - insets.getBottom(),
                pane.getHeight());
        Rectangle2D rect = new Rectangle2D(
                insets.getLeft(),
                insets.getTop(),
                width,
                height);
        profileRectangleProperty.set(rect);
    }

}
