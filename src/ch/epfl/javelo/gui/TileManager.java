package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class allows access to OSM tiles through download or loading from cache.
 *
 * @author Edouard Mignan (345875)
 */
public final class TileManager {

    private final LinkedHashMap<TileId, Image> memoryCache;
    private final Path cachePath;
    private final String tileServerName;
    private final int CACHE_SIZE = 100;
    private final static int BUFFERED_STREAM_READ_LIMIT = 256 * 1024;
    // 256kB read limit >>>> image size

    /**
     * Creates a Tile Manager to download OSM tiles and store them in cache.
     *
     * @param cachePath      the path to the disk cache
     * @param tileServerName the name of the OSM server to download tiles from
     */
    public TileManager(Path cachePath, String tileServerName) {
        memoryCache = new LinkedHashMap<>(CACHE_SIZE, 0.75F, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            } /* This automatically replaces last accessed element by newest
                 element being put if the map is at the maximum allowed size. */
        };

        this.cachePath = cachePath;
        this.tileServerName = tileServerName;
    }

    /**
     * Returns the image of the tile given in argument.
     *
     * @param tileId the tile of which to download the image
     * @return the image of the tile
     * @throws IOException if there is an input output problem
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        Preconditions.checkArgument(
                TileId.isValid(tileId.zoomLevel, tileId.xIndex, tileId.yIndex));

        if (memoryCache.containsKey(tileId))
            return memoryCache.get(tileId);

        String zlString = Integer.toString(tileId.zoomLevel);
        String xString = Integer.toString(tileId.xIndex);
        String yFileString = tileId.yIndex + ".png";

        Path xDirectory = cachePath.resolve(zlString).resolve(xString);
        Path filePath = xDirectory.resolve(yFileString);

        if (Files.exists(filePath)) {
            try (InputStream input =
                         new BufferedInputStream(
                                 new FileInputStream(
                                         filePath.toFile()))) {
                Image image = new Image(input);
                memoryCache.put(tileId, image);
                return image;
            }
        }

        String urlFile = zlString + "/" + xString + "/" + yFileString;
        URL url = new URL("https", tileServerName, urlFile);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "JaVelo");

        try (InputStream i = new BufferedInputStream(connection.getInputStream())) {
            Files.createDirectories(xDirectory);
            Files.createFile(filePath);

            i.mark(BUFFERED_STREAM_READ_LIMIT);
            Image tileImage = new Image(i);
            i.reset();

            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                i.transferTo(outputStream);
            }

            memoryCache.put(tileId, tileImage);
            return tileImage;
        }
    }

    /**
     * Represents the identity of a tile.
     *
     * @param zoomLevel the zoom level of the tile
     * @param xIndex    the index on the X axis of the tile
     * @param yIndex    the index on the Y axis of the tile
     */
    record TileId(int zoomLevel, int xIndex, int yIndex) {

        /**
         * Asserts if the tile attributes passed as arguments are those of a valid tile.
         *
         * @param zoomLevel the zoom level of the tile
         * @param xIndex    the index on the X axis of the tile
         * @param yIndex    the index on the Y axis of the tile
         * @return true if the tile is valid, false otherwise
         */
        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            return zoomLevel <= 20
                    && 0 <= xIndex && xIndex < Math.scalb(1, zoomLevel)
                    && 0 <= yIndex && yIndex < Math.scalb(1, zoomLevel);
        }
    }
}
