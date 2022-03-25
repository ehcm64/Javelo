package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleRouteTest {

    @Test
    void pointAt() throws IOException {

        var p1 = new PointCh(2600123, 1200456);
        var p2 = new PointCh(2600456, 1200789);
        var p3 = new PointCh(2600789, 1200123);
        var p4 = new PointCh(2601000, 1201000);

        var edge1 = new Edge(1, 2, p1, p2, p1.distanceTo(p2), x -> Double.NaN);
        var edge2 = new Edge(3, 4, p3, p4, p3.distanceTo(p4), x -> Double.NaN);

        List<Edge> points = new ArrayList<>();

        points.add(edge1);
        points.add(edge2);

        SingleRoute route = new SingleRoute(points);

        assertEquals(edge1.fromPoint(),route.pointAt(-5));
        assertEquals(edge1.fromPoint(),route.pointAt(0));
        assertEquals(edge2.toPoint(),route.pointAt(200000000));
        assertEquals(edge2.toPoint(), route.pointAt(p1.distanceTo(p2) + p3.distanceTo(p4)));

        for(int i = 1; i <= 10; i++){
            assertEquals(new PointCh(2600123 + 333./i, 1200456 + 333./i), route.pointAt(p1.distanceTo(p2)/i));
            assertEquals(new PointCh(2600789 + 211./i, 1200123 + 877./i), route.pointAt(p1.distanceTo(p2)+p3.distanceTo(p4)/i));
        }






    }

    @Test
    void elevationAt() {
    }

    @Test
    void nodeClosestTo() {
    }

    @Test
    void pointClosestTo() {
    }
}