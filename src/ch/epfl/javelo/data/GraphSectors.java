package ch.epfl.javelo.data;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the array containing the 16384 sectors of JaVelo
 *
 * @author Timo Moebel (345665)
 */
public record GraphSectors(ByteBuffer buffer) {
    private static final short OFFSET_SHORT = Short.BYTES;
    private static final int OFFSET_INTEGER = Integer.BYTES;
    private static final int OFFSET_SUM = OFFSET_INTEGER + OFFSET_SHORT;

    /**
     * Represents a sector with 2 nodes (the first and last)
     */
    public record Sector(int startNodeId, int endNodeId) {

    }

    private static int xToSectorCoord(double e) {
        double x = 128 / SwissBounds.WIDTH * e - 128 * SwissBounds.MIN_E / SwissBounds.WIDTH;
        return (int) x;

    }

    private static int yToSectorCoords(double n) {
        double y = 128 / SwissBounds.HEIGHT * n - 128 * SwissBounds.MIN_N / SwissBounds.HEIGHT;
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
        double eCoord = center.e();
        double nCoord = center.n();
        int eMin = xToSectorCoord(eCoord - distance);
        int eMax = xToSectorCoord(eCoord + distance);
        int nMin = yToSectorCoords(nCoord - distance);
        int nMax = yToSectorCoords(nCoord + distance);

        for (int y = nMin; y <= nMax; y++) {
            for (int x = eMin; x <= eMax; x++) {
                if (y < 0 || x < 0 || y > 127 || x > 127) continue;

                int index = 128 * y + x;
                int firstNode = buffer.getInt(index * OFFSET_SUM);
                int nodesNumber = buffer().getShort(index * OFFSET_SUM + OFFSET_INTEGER);
                int lastNode = firstNode + nodesNumber;
                Sector s = new Sector(firstNode, lastNode);
                sectors.add(s);
            }
        }
        return sectors;
    }
}