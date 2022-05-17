package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.routing.ElevationProfile;
import com.sun.javafx.geom.Vec2d;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.*;

import java.util.Arrays;


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

    private static final Insets insets = new Insets(10, 10, 20, 40);
    private static final int[] POS_STEPS =
            {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
    private static final int[] ELE_STEPS =
            {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};
    private static final int MIN_VERTICAL_OFFSET = 50;
    private static final int MIN_HORIZONTAL_OFFSET = 25;


    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profileProperty,
                                   ReadOnlyDoubleProperty positionProperty) {

        this.profileProperty = profileProperty;
        this.positionProperty = positionProperty;
        mousePositionProperty = new SimpleDoubleProperty();

        screenToWorldProperty = new SimpleObjectProperty<>();
        worldToScreenProperty = new SimpleObjectProperty<>();


        borderPane = new BorderPane();
        pane = new Pane();
        path = new Path();


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
        pane.getChildren().addAll(group, polygon, path, line);

        borderPane.setCenter(pane);

        vBox = new VBox();
        text = new Text();
        createBottomText();

        borderPane.getStylesheets().add("elevation_profile.css");

        profileRectangleProperty = new SimpleObjectProperty<>();
        profileRectangleProperty.set(new Rectangle2D(0, 0, 0, 0));
        addListeners();

        createTransforms();
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
            createTransforms();
            graphProfile();
            createGrid();
        });

        pane.heightProperty().addListener((p, o, n) -> {
            resizeRectangle();
            createTransforms();
            graphProfile();
            createGrid();
        });
    }

    private void graphProfile() {
        Rectangle2D rect = profileRectangleProperty.get();
        ElevationProfile ep = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();
        double step = ep.length() / (rect.getWidth());
        polygon.getPoints().clear();
        polygon.getPoints().addAll(rect.getMinX(), rect.getMaxY());
        for (double i = 0; i < ep.length(); i += step) {
            Point2D worldPoint = new Point2D(i, ep.elevationAt(i));
            Point2D screenPoint = worldToScreen.transform(worldPoint);
            polygon.getPoints().addAll(
                    screenPoint.getX(),
                    screenPoint.getY());
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

    private void createTransforms() {
        Affine screenToWorld = new Affine();
        ElevationProfile ep = profileProperty.get();
        Rectangle2D rect = profileRectangleProperty.get();
        double elevationDelta = ep.maxElevation() - ep.minElevation();
        screenToWorld.prependTranslation(-rect.getMinX(), -rect.getMaxY());
        screenToWorld.prependScale(
                ep.length() / rect.getWidth(),
                -elevationDelta / rect.getHeight());
        screenToWorld.prependTranslation(0, ep.minElevation());
        screenToWorldProperty.set(screenToWorld);
        try {
            Affine worldToScreen = screenToWorld.createInverse();
            worldToScreenProperty.set(worldToScreen);
        } catch (NonInvertibleTransformException ignored) {
        }
    }

    private void createBottomText() {
        ElevationProfile ep = profileProperty.get();
        text.setText(String.format("Longueur : %.1f km" +
                        "     Montée : %.0f m" +
                        "     Descente : %.0f m" +
                        "     Altitude : de %.0f m à %.0f m",
                ep.length(),
                ep.totalAscent(),
                ep.totalDescent(),
                ep.minElevation(),
                ep.maxElevation()));
        vBox.getChildren().add(text);
        vBox.setId("profile_data");
        borderPane.setBottom(vBox);
    }

    private void createGrid() {
        path.setId("grid");
        path.getElements().clear();
        Transform worldToScreen = worldToScreenProperty.get();
        Rectangle2D rect = profileRectangleProperty.get();
        ElevationProfile ep = profileProperty.get();
        Point2D verticalPt = new Point2D(rect.getMinX() + MIN_VERTICAL_OFFSET, rect.getMaxY());
        verticalPt = screenToWorldProperty.get().transform(verticalPt);

        int vIndex = Arrays.binarySearch(POS_STEPS, (int) Math.ceil(verticalPt.getX()));
        vIndex = vIndex < 0 ? -vIndex - 1 : vIndex;
        int step = POS_STEPS[vIndex];
        for (int pos = 0; pos <= ep.length(); pos += step) {
            Point2D movePt = worldToScreen.transform(new Point2D(pos, ep.minElevation()));
            PathElement moveTo = new MoveTo(movePt.getX(), movePt.getY());
            Point2D linePt = worldToScreen.transform(new Point2D(pos, ep.maxElevation()));
            PathElement lineTo = new LineTo(linePt.getX(), linePt.getY());
            path.getElements().addAll(moveTo, lineTo);
        }
        Point2D pt = new Point2D(rect.getMinX(), rect.getMaxY() - MIN_HORIZONTAL_OFFSET);
        pt = screenToWorldProperty.get().transform(pt);
        System.out.println(pt.getY());
        int hIndex = Arrays.binarySearch(ELE_STEPS, (int) Math.ceil(pt.getY() - ep.minElevation()));
        hIndex = hIndex < 0 ? -hIndex - 1 : hIndex;
        hIndex = hIndex == ELE_STEPS.length ? hIndex - 1 : hIndex;
        step = ELE_STEPS[hIndex];
        System.out.println(step);
        int smallestEle = (int) Math.ceil(ep.minElevation() / step) * step;
        for (int ele = smallestEle; ele <= ep.maxElevation(); ele += step) {
            Point2D movePt = worldToScreen.transform(new Point2D(0, ele));
            PathElement moveTo = new MoveTo(movePt.getX(), movePt.getY());
            Point2D linePt = worldToScreen.transform(new Point2D(ep.length(), ele));
            PathElement lineTo = new LineTo(linePt.getX(), linePt.getY());
            path.getElements().addAll(moveTo, lineTo);
        }
    }

    private void setEtiquettes() {

    }
}
