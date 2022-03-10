package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Short.toUnsignedInt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphSectorsTest {

    @Test
    void checkWithValueTest() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0, 0, 0, 16, 0, 20});
        GraphSectors ns = new GraphSectors(buffer);
        int idStartNode = buffer.getInt(0);
        int idEndNode = idStartNode + toUnsignedInt(buffer.getShort(4));
        GraphSectors.Sector sector = new GraphSectors.Sector(idStartNode, idEndNode);
        List<GraphSectors.Sector> expected0 = new ArrayList<>();
        expected0.add(sector);
        List<GraphSectors.Sector> actual0 = ns.sectorsInArea(new PointCh(2485100, 1075100), 100);
        assertEquals(expected0, actual0);

        ByteBuffer buffer1 = ByteBuffer.wrap(new byte[]{0, 0, 0, 16, 0, 20, 0, 0, 0, 21, 0, 12});
        GraphSectors ns1 = new GraphSectors(buffer1);
        int idEndNode1 = buffer1.getInt(6) + toUnsignedInt(buffer1.getShort(10));
        GraphSectors.Sector sector1 = new GraphSectors.Sector(buffer1.getInt(6), idEndNode1);
        List<GraphSectors.Sector> expected1 = new ArrayList<GraphSectors.Sector>();
        expected1.add(sector1);
        List<GraphSectors.Sector> actual1 = ns1.sectorsInArea(new PointCh(2488050, 1076050), 50);
        assertEquals(expected1, actual1);
    }

    @Test
    void GraphsSectorsWorksTrivial() {
        byte[] tab = new byte[48];
        for (byte i = 0; i < 48; i++) {
            tab[i] = i;
        }
        ByteBuffer b = ByteBuffer.wrap(tab);
        List<GraphSectors.Sector> output = new ArrayList<GraphSectors.Sector>();

    }

    @Test
    void GraphSectorsWorksWith00() {

        byte[] tab = new byte[98304];

        for (int i = 0; i < 98304; i += 6) {

            tab[i] = (byte) Bits.extractUnsigned(i * 4, 24, 8);
            tab[i + 1] = (byte) Bits.extractUnsigned(i * 4, 16, 8);
            tab[i + 2] = (byte) Bits.extractUnsigned(i * 4, 8, 8);
            tab[i + 3] = (byte) Bits.extractUnsigned(i * 4, 0, 8);

            tab[i + 4] = (byte) 0;
            tab[i + 5] = (byte) 1;

        }

        ByteBuffer buffer = ByteBuffer.wrap(tab);

        GraphSectors graph = new GraphSectors(buffer);

        ArrayList<GraphSectors.Sector> output = new ArrayList<>();
        output.add(new GraphSectors.Sector(0, 1));

        List<GraphSectors.Sector> actual = graph.sectorsInArea(new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N), 5);
        assertEquals(output.get(0), actual.get(0));

    }

    @Test
    void GraphSectorsWorksWithExpected() {

        byte[] tab = new byte[98304];

        for (int i = 0; i < 16384; i++) {

            tab[i * 6] = (byte) Bits.extractUnsigned(i * 4, 24, 8);
            tab[6 * i + 1] = (byte) Bits.extractUnsigned(i * 4, 16, 8);
            tab[6 * i + 2] = (byte) Bits.extractUnsigned(i * 4, 8, 8);
            tab[6 * i + 3] = (byte) Bits.extractUnsigned(i * 4, 0, 8);

            tab[6 * i + 4] = (byte) 0;
            tab[6 * i + 5] = (byte) 4;

        }

        ByteBuffer buffer = ByteBuffer.wrap(tab);

        GraphSectors graph = new GraphSectors(buffer);

        ArrayList<GraphSectors.Sector> output = new ArrayList<>();

        for (int i = 0; i < 384; i += 128) {
            for (int j = 0; j < 3; j++) {
                output.add(new GraphSectors.Sector((j + i) * 4, (j + i + 1) * 4));
            }
        }
        List<GraphSectors.Sector> actual = graph.sectorsInArea(new PointCh(SwissBounds.MIN_E + 3700, SwissBounds.MIN_N + 2500), 2000);
        assertArrayEquals(output.toArray(), actual.toArray());
    }

    @Test
    void GraphSectorsWorksWithEntireMap() {
        byte[] tab = new byte[98304];

        for (int i = 0; i < 16384; i++) {

            tab[i * 6] = (byte) Bits.extractUnsigned(i * 4, 24, 8);
            tab[6 * i + 1] = (byte) Bits.extractUnsigned(i * 4, 16, 8);
            tab[6 * i + 2] = (byte) Bits.extractUnsigned(i * 4, 8, 8);
            tab[6 * i + 3] = (byte) Bits.extractUnsigned(i * 4, 0, 8);

            tab[6 * i + 4] = (byte) 0;
            tab[6 * i + 5] = (byte) 4;

        }

        ByteBuffer buffer = ByteBuffer.wrap(tab);
        GraphSectors graph = new GraphSectors(buffer);
        ArrayList<GraphSectors.Sector> output = new ArrayList<>();

        for (int j = 0; j < 128; j++) {
            for (int i = 0; i < 128; i++) {
                output.add(new GraphSectors.Sector((j * 128 + i) * 4, (j * 128 + i + 1) * 4));
            }
        }
        List<GraphSectors.Sector> actual = graph.sectorsInArea(new PointCh(SwissBounds.MIN_E + SwissBounds.WIDTH / 2 + 10, SwissBounds.MIN_N + SwissBounds.HEIGHT / 2 + 10), SwissBounds.WIDTH);
        System.out.println(output.size() + " " + actual.size());
        assertArrayEquals(output.toArray(), actual.toArray());
    }
}