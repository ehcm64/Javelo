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
 * Represents an OSM tile manager
 * @author Edouard Mignan (345875)
 * @author Timo Moebel (345665)
 */
public final class TileManager {
    private LinkedHashMap<TileId, Image> memoryCache;
    private final Path cachePath;
    private final String tileServerName;
    private final int CACHE_SIZE = 100;

    /**
     * Constructs a tile manager
     * @param cachePath the path to the directory containing the disk cache
     * @param tileServerName the name of the tile server
     */
    public TileManager(Path cachePath, String tileServerName) {
        memoryCache = new LinkedHashMap<>(CACHE_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };
        this.cachePath = cachePath;
        this.tileServerName = tileServerName;
    }

    /**
     * Returns an image from a tile
     * @param tileId identity of a tile
     * @return an image from this tile
     * @throws IOException if a stream throws an exception
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

            i.mark(256 * 1024); // 256kB read limit >>>> image size
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
     * Represents a tile OSM
     * @param zoomLevel zoom level of the tile
     * @param xIndex X index of the tile
     * @param yIndex Y index of the tile
     */
    record TileId(int zoomLevel, int xIndex, int yIndex) {

        /**
         * Checks if a tile is valid or not
         * @param zoomLevel zoom level of the tile
         * @param xIndex X index of the tile
         * @param yIndex Y index of the tile
         * @return true if the tile is valid and false otherwise
         */
        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            return zoomLevel <= 20
                    && 0 <= xIndex && xIndex < Math.scalb(1, zoomLevel)
                    && 0 <= yIndex && yIndex < Math.scalb(1, zoomLevel);
        }
    }
}
