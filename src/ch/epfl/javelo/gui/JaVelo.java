package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.RouteComputer;
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

import java.nio.file.Path;
import java.util.function.Consumer;

public final class JaVelo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {



        Graph graph = Graph.loadFrom(Path.of("lausanne"));
        Path cachePath = Path.of("cache");
        String tileServerName = "tile.openstreetmap.org";
        CostFunction cf = new CityBikeCF(graph);
        String title = "JaVelo";

        BorderPane mainPane = new BorderPane();


        TileManager tm = new TileManager(cachePath, tileServerName);
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

        SplitPane mapAndProfile = new SplitPane(amm.pane());
        mapAndProfile.setOrientation(Orientation.VERTICAL);
        routeBean.getElevationProfile().addListener(p -> {
            if (p == null) {
                mapAndProfile.getItems().clear();
                mapAndProfile.getItems().addAll(amm.pane());
            } else {
                ElevationProfileManager epm = new ElevationProfileManager(
                        routeBean.getElevationProfile(),
                        routeBean.highlightedPositionProperty());
                Pane profilePane = epm.pane();
                mapAndProfile.getItems().add(profilePane);
                SplitPane.setResizableWithParent(profilePane, false);
                routeBean.highlightedPositionProperty().bind(Bindings.createDoubleBinding(() -> {
                    if (!Double.isNaN(epm.mousePositionOnProfileProperty().doubleValue()))
                        return epm.mousePositionOnProfileProperty().doubleValue();
                    else if (!Double.isNaN(amm.mousePositionOnRouteProperty().doubleValue()))
                        return amm.mousePositionOnRouteProperty().doubleValue();
                    else return null;
                }, epm.mousePositionOnProfileProperty(), amm.mousePositionOnRouteProperty()));
            }
        });

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Fichier");
        MenuItem menuItem = new MenuItem("Exporter GPX");
        menuItem.disableProperty().bind(BooleanBinding.booleanExpression(routeBean.getRoute().isNull()));
        menu.getItems().add(menuItem);
        menuBar.getMenus().add(menu);

        StackPane stackPane = new StackPane(mapAndProfile, errorManager.pane());
        mainPane.setCenter(stackPane);
        mainPane.setTop(menuBar);

        Scene scene = new Scene(mainPane);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


}
