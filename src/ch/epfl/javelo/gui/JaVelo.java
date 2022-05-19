package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class JaVelo extends Application {

    private static final Path GRAPH_DATA_PATH = Path.of("javelo-data");
    private static final Path CACHE_PATH = Path.of("osm-cache");
    private static final String TILE_SERVER_NAME = "tile.openstreetmap.org";
    private static final String WINDOW_NAME = "JaVelo";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Graph graph = Graph.loadFrom(GRAPH_DATA_PATH);
        CostFunction cf = new CityBikeCF(graph);

        BorderPane mainPane = new BorderPane();


        TileManager tm = new TileManager(CACHE_PATH, TILE_SERVER_NAME);
        RouteComputer rc = new RouteComputer(graph, cf);
        RouteBean routeBean = new RouteBean(rc);
        ErrorManager errorManager = new ErrorManager();

        final class ErrorConsumer
                implements Consumer<String> {
            @Override
            public void accept(String s) {
                errorManager.displayError(s);
            }
        }
        ErrorConsumer errorConsumer = new ErrorConsumer();


        AnnotatedMapManager amm = new AnnotatedMapManager(graph, tm, routeBean, errorConsumer);
        ElevationProfileManager epm = new ElevationProfileManager(
                routeBean.getElevationProfile(),
                routeBean.highlightedPositionProperty());
        Pane profilePane = epm.pane();

        SplitPane mapAndProfile = new SplitPane(amm.pane());
        mapAndProfile.setOrientation(Orientation.VERTICAL);

        routeBean.highlightedPositionProperty().bind(Bindings.createDoubleBinding(() -> {
            if (!Double.isNaN(epm.mousePositionOnProfileProperty().doubleValue()))
                return epm.mousePositionOnProfileProperty().doubleValue();
            else if (!Double.isNaN(amm.mousePositionOnRouteProperty().doubleValue()))
                return amm.mousePositionOnRouteProperty().doubleValue();
            else return null;
        }, epm.mousePositionOnProfileProperty(), amm.mousePositionOnRouteProperty()));

        routeBean.getElevationProfile().addListener(p -> {
            ElevationProfile ep = routeBean.elevationProfile();
            if (ep == null) {
                mapAndProfile.getItems().remove(profilePane);
            } else {
                mapAndProfile.getItems().add(profilePane);
                SplitPane.setResizableWithParent(profilePane, false);
            }
        });

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Fichier");
        MenuItem menuItem = new MenuItem("Exporter GPX");
        menuItem.disableProperty().bind(BooleanBinding.booleanExpression(routeBean.getRoute().isNull()));
        menuItem.setOnAction(e -> {
            try {
                GpxGenerator.writeGpx(
                        "javelo.gpx",
                        routeBean.route(),
                        routeBean.elevationProfile());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        menu.getItems().add(menuItem);
        menuBar.getMenus().add(menu);

        StackPane stackPane = new StackPane(mapAndProfile, errorManager.pane());
        mainPane.setCenter(stackPane);
        mainPane.setTop(menuBar);

        Scene scene = new Scene(mainPane);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle(WINDOW_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


}
