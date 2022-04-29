package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;


public final class RouteBean {
    private final ObjectProperty<RouteComputer> rteComp;
    private ObservableList<Waypoint> wpts;
    private ObjectProperty<Route> route;
    private DoubleProperty highlightedPosition;
    private ObjectProperty<ElevationProfile> elevationProfile;

    public RouteBean(RouteComputer rteComp) {
        this.rteComp = new SimpleObjectProperty<>();
        this.rteComp.set(rteComp);
    }

    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route;
    }



}
