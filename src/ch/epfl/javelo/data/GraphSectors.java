package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the array containing the sectors of JaVelo
 *
 * @author Timo Moebel (345665)
 */
public record GraphSectors(ByteBuffer buffer) {
    private static final short OFFSET_SHORT = Short.BYTES;
    private static final int OFFSET_INTEGER = Integer.BYTES;
    private static final int OFFSET_SUM = OFFSET_INTEGER + OFFSET_SHORT;

    private static final int NB_OF_SECTORS_PER_SIDE = 128;

    /**
     * Represents a sector with 2 nodes (the first and last)
     */
    public record Sector(int startNodeId, int endNodeId) {
    }

    private static int xToSectorCoords(double e) {
        double x = NB_OF_SECTORS_PER_SIDE / SwissBounds.WIDTH * e
                - NB_OF_SECTORS_PER_SIDE * SwissBounds.MIN_E / SwissBounds.WIDTH;
        return (int) x;

    }

    private static int yToSectorCoords(double n) {
        double y = NB_OF_SECTORS_PER_SIDE / SwissBounds.HEIGHT * n
                - NB_OF_SECTORS_PER_SIDE * SwissBounds.MIN_N / SwissBounds.HEIGHT;
        return (int) y;

    }

    /**
     * Returns the list of all sectors having an intersection with the centered square around a given point.
     *
     * @param center   the center of the square
     * @param distance half the size of one side
     * @return a list of sectors
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {
        List<Sector> sectors = new ArrayList<>();

        int eMin = xToSectorCoords(Math2.clamp(
                SwissBounds.MIN_E + 1, center.e() - distance, SwissBounds.MAX_E - 1));

        int eMax = xToSectorCoords(Math2.clamp(
                SwissBounds.MIN_E + 1, center.e() + distance, SwissBounds.MAX_E - 1));

        int nMin = yToSectorCoords(Math2.clamp(
                SwissBounds.MIN_N + 1, center.n() - distance, SwissBounds.MAX_N - 1));

        int nMax = yToSectorCoords(Math2.clamp(
                SwissBounds.MIN_N + 1, center.n() + distance, SwissBounds.MAX_N - 1));


        for (int y = nMin; y <= nMax; y++) {
            for (int x = eMin; x <= eMax; x++) {
                int index = NB_OF_SECTORS_PER_SIDE * y + x;
                int firstNode = buffer.getInt(index * OFFSET_SUM);
                int nodesNumber = Short.toUnsignedInt(
                        buffer().getShort(
                                index
                                        * OFFSET_SUM
                                        + OFFSET_INTEGER));
                int lastNode = firstNode + nodesNumber;
                Sector s = new Sector(firstNode, lastNode);
                sectors.add(s);
            }
        }
        return sectors;
    }
}