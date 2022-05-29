package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.*;

import java.util.Arrays;

/**
 * Represents the part of the GUI that displays the elevation profile.
 *
 * @author Edouard Mignan (345875)
 */
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

    private static final Insets INSETS = new Insets(10, 10, 20, 40);
    private static final int[] POS_STEPS =
            {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
    private static final int[] ELE_STEPS =
            {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};

    private static final int MIN_VERTICAL_OFFSET = 50;
    private static final int MIN_HORIZONTAL_OFFSET = 25;
    private static final Font FONT = Font.font("Avenir", 10);
    private static final int METERS_IN_KILOMETER = 1000;

    /**
     * Creates an elevation profile manager.
     *
     * @param profileProperty the property containing the elevation profile
     * @param positionProperty the property containing the position
     *                         to highlight on the profile
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> profileProperty,
                                   ReadOnlyDoubleProperty positionProperty) {

        this.profileProperty = profileProperty;
        this.positionProperty = positionProperty;
        mousePositionProperty = new SimpleDoubleProperty(Double.NaN);

        screenToWorldProperty = new SimpleObjectProperty<>();
        worldToScreenProperty = new SimpleObjectProperty<>();
        profileRectangleProperty = new SimpleObjectProperty<>();

        borderPane = new BorderPane();

        vBox = new VBox();
        text = new Text();
        vBox.getChildren().add(text);
        vBox.setId("profile_data");

        pane = new Pane();
        path = new Path();
        path.setId("grid");
        group = new Group();
        polygon = new Polygon();
        polygon.setId("profile");
        line = new Line();

        pane.getChildren().addAll(group, polygon, path, line);

        borderPane.setCenter(pane);
        borderPane.setBottom(vBox);
        borderPane.getStylesheets().add("elevation_profile.css");

        addBinds();
        addListeners();
        addEvents();
    }

    /**
     * Returns a pane containing the displayed elevation profile.
     *
     * @return the pane
     */
    public Pane pane() {
        return borderPane;
    }

    /**
     * Returns a read-only property containing the position (in meters) highlighted
     * by the mouse on the profile.
     *
     * @return the property
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionProperty;
    }

    private void addBinds() {

        profileRectangleProperty.bind(Bindings.createObjectBinding(() -> {
            double width = Math2.clamp(
                    0,
                    pane.getWidth() - INSETS.getRight() - INSETS.getLeft(),
                    pane.getWidth());
            double height = Math2.clamp(
                    0,
                    pane.getHeight() - INSETS.getTop() - INSETS.getBottom(),
                    pane.getHeight());
            return new Rectangle2D(
                    INSETS.getLeft(),
                    INSETS.getTop(),
                    width,
                    height);
        }, pane.widthProperty(), pane.heightProperty()));

        screenToWorldProperty.bind(Bindings.createObjectBinding(() -> {
            ElevationProfile ep = profileProperty.get();
            Affine screenToWorld = new Affine();
            if (ep == null) return screenToWorld;
            Rectangle2D rect = profileRectangleProperty.get();
            double elevationDelta = ep.maxElevation() - ep.minElevation();
            screenToWorld.prependTranslation(-rect.getMinX(), -rect.getMaxY());
            screenToWorld.prependScale(
                    ep.length() / rect.getWidth(),
                    -elevationDelta / rect.getHeight());
            screenToWorld.prependTranslation(0, ep.minElevation());
            return screenToWorld;
        }, profileRectangleProperty, profileProperty));

        worldToScreenProperty.bind(Bindings.createObjectBinding(() -> {
            try {
                return screenToWorldProperty.get().createInverse();
            } catch (NonInvertibleTransformException ignored) {}
            return new Affine();
        }, screenToWorldProperty));

        line.layoutXProperty().bind(
                Bindings.createDoubleBinding(() ->
                        worldToScreenProperty
                                .get()
                                .transform(
                                        new Point2D(positionProperty.doubleValue(), 0))
                                .getX(), positionProperty, worldToScreenProperty));

        line.startYProperty().bind(
                Bindings.select(profileRectangleProperty, "minY"));

        line.endYProperty().bind(
                Bindings.select(profileRectangleProperty, "maxY"));

        line.visibleProperty().bind(
                BooleanBinding.booleanExpression(
                        positionProperty.greaterThanOrEqualTo(0)));

        text.textProperty().bind(Bindings.createStringBinding(() -> {
            ElevationProfile ep = profileProperty.get();
            if (ep == null) return "";
            return String.format("Longueur : %.1f km" +
                            "     Montée : %.0f m" +
                            "     Descente : %.0f m" +
                            "     Altitude : de %.0f m à %.0f m",
                    ep.length() / METERS_IN_KILOMETER,
                    ep.totalAscent(),
                    ep.totalDescent(),
                    ep.minElevation(),
                    ep.maxElevation());
        }, profileProperty));
    }

    private void addEvents() {
        pane.setOnMouseMoved(e -> {
            Point2D screen = new Point2D(e.getX(), 0);
            Point2D world = screenToWorldProperty.get().transform(screen);
            mousePositionProperty.set(world.getX());
        });

        pane.setOnMouseExited(e -> mousePositionProperty.set(Double.NaN));
    }

    private void addListeners() {
        profileProperty.addListener(p -> {
            ElevationProfile ep = profileProperty.get();
            if (ep == null) return;
            graphProfile();
            createGridAndEtiquettes();
        });

        pane.widthProperty().addListener(p -> {
            ElevationProfile ep = profileProperty.get();
            if (ep == null) return;
            graphProfile();
            createGridAndEtiquettes();
        });

        pane.heightProperty().addListener(p -> {
            ElevationProfile ep = profileProperty.get();
            if (ep == null) return;
            graphProfile();
            createGridAndEtiquettes();
        });
    }

    private void graphProfile() {
        Rectangle2D rect = profileRectangleProperty.get();
        ElevationProfile ep = profileProperty.get();
        Transform worldToScreen = worldToScreenProperty.get();
        double step = ep.length() / (rect.getWidth());
        polygon.getPoints().clear();
        polygon.getPoints().addAll(rect.getMinX(), rect.getMaxY());
        for (double i = 0; i <= ep.length(); i += step) {
            Point2D worldPoint = new Point2D(i, ep.elevationAt(i));
            Point2D screenPoint = worldToScreen.transform(worldPoint);
            polygon.getPoints().addAll(
                    screenPoint.getX(),
                    screenPoint.getY());
        }
        polygon.getPoints().addAll(rect.getMaxX(), rect.getMaxY());
    }

    private void createGridAndEtiquettes() {
        path.getElements().clear();
        group.getChildren().clear();
        ElevationProfile ep = profileProperty.get();
        Rectangle2D rect = profileRectangleProperty.get();
        if (rect.getWidth() == 0 || rect.getHeight() == 0) return;
        Transform worldToScreen = worldToScreenProperty.get();
        Transform screenToWorld = screenToWorldProperty.get();

        Point2D linesGap = screenToWorld.transform(
                rect.getMinX() + MIN_VERTICAL_OFFSET,
                rect.getMaxY() - MIN_HORIZONTAL_OFFSET);

        int vIndex = Arrays.binarySearch(POS_STEPS, (int) linesGap.getX());
        if (vIndex < 0) vIndex = Math2.clamp(0, -vIndex - 1, POS_STEPS.length - 1);
        int vStep = POS_STEPS[vIndex];

        for (int pos = 0; pos <= ep.length(); pos += vStep) {
            Point2D movePt = worldToScreen.transform(new Point2D(pos, ep.minElevation()));
            PathElement moveTo = new MoveTo(movePt.getX(), movePt.getY());
            Point2D linePt = worldToScreen.transform(new Point2D(pos, ep.maxElevation()));
            PathElement lineTo = new LineTo(linePt.getX(), linePt.getY());
            path.getElements().addAll(moveTo, lineTo);

            createPositionEtiquette(pos, movePt.getX(), movePt.getY());
        }

        int hIndex = Arrays.binarySearch(ELE_STEPS, (int) (linesGap.getY() - ep.minElevation()));
        if (hIndex < 0) hIndex = Math2.clamp(0, -hIndex - 1, ELE_STEPS.length - 1);
        int hStep = ELE_STEPS[hIndex];

        int smallestEleMultiple = (int) Math.ceil(ep.minElevation() / hStep) * hStep;

        for (int ele = smallestEleMultiple; ele <= ep.maxElevation(); ele += hStep) {
            Point2D movePt = worldToScreen.transform(new Point2D(0, ele));
            PathElement moveTo = new MoveTo(movePt.getX(), movePt.getY());
            Point2D linePt = worldToScreen.transform(new Point2D(ep.length(), ele));
            PathElement lineTo = new LineTo(linePt.getX(), linePt.getY());
            path.getElements().addAll(moveTo, lineTo);

            createElevationEtiquette(ele, movePt.getX(), movePt.getY());
        }
    }

    private void createPositionEtiquette(double position, double x, double y) {
        Text posText = new Text(Integer.toString((int) position / METERS_IN_KILOMETER));
        group.getChildren().add(posText);
        posText.getStyleClass().addAll("grid_label", "horizontal");
        posText.setFont(FONT);
        posText.setTextOrigin(VPos.TOP);
        double centering = 0.5 * posText.prefWidth(0);
        posText.setLayoutX(x - centering);
        posText.setLayoutY(y);
    }

    private void createElevationEtiquette(double elevation, double x, double y) {
        Text eleText = new Text(Integer.toString((int) elevation));
        group.getChildren().add(eleText);
        eleText.getStyleClass().addAll("grid_label", "vertical");
        eleText.setFont(FONT);
        eleText.setTextOrigin(VPos.CENTER);
        double centering = eleText.prefWidth(0) + 2;
        eleText.setLayoutX(x - centering);
        eleText.setLayoutY(y);
    }
}
