package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileManager {
    private LinkedHashMap<TileId, Image> memoryCache;
    private final Path cachePath;
    private final String tileServerName;

    public TileManager(Path cachePath, String tileServerName) {
        this.memoryCache = new LinkedHashMap<>(100, 0.75F, true) {
            private static final int MAX_ENTRIES = 100;
            protected boolean removeEldestEntry(Map.Entry<TileId, Image> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
        this.cachePath = cachePath;
        this.tileServerName = tileServerName;
    }

    public Image imageForTileAt(TileId tileId) throws IOException {
        if (!TileId.isValid(tileId.zoomLevel, tileId.xIndex, tileId.yIndex))
            throw new IllegalArgumentException();
        if (memoryCache.containsKey(tileId))
            return memoryCache.get(tileId);
        String zlString = "/" + tileId.zoomLevel;
        String xString = "/" + tileId.xIndex;
        String yString = "/" + tileId.yIndex;
        Path zlDir = Path.of(cachePath.toString(), "/" + zlString);
        Path xDir = Path.of(zlDir.toString(), "/" + xString);
        Path filePath = Path.of(xDir.toString(), "/" + yString + ".png");
        if (Files.exists(filePath)) {
            try (InputStream input = new FileInputStream(filePath.toFile())) {
                return new Image(input);
            }
        }
        URL url = new URL("http://" + this.tileServerName + zlString + xString + yString + ".png");
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "JaVelo");
        try (InputStream i = new BufferedInputStream(connection.getInputStream())) {
            Files.createDirectory(cachePath);
            Files.createDirectories(xDir);
            Files.createFile(filePath);
            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                i.transferTo(outputStream);
                try (InputStream j = new FileInputStream(filePath.toFile())) {
                    Image tileImage = new Image(j);
                    memoryCache.put(tileId, tileImage);
                    return tileImage;
                }
            }
        }
    }

    record TileId(int zoomLevel, int xIndex, int yIndex) {

        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            PointWebMercator point = PointWebMercator.of(zoomLevel, xIndex, yIndex);
            return (0 <= point.x() && point.x() <= 1)
                    &&
                    (0 <= point.y() && point.y() <= 1);
        }
    }
}
