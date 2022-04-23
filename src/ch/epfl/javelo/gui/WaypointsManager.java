package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import javafx.beans.property.Property;

import java.util.List;
import java.util.function.Consumer;

public final class WaypointsManager {

    public WaypointsManager(Graph graph,
                            Property mapParameters,
                            List<Waypoint> waypoints,
                            Consumer<String> error) {}

}
