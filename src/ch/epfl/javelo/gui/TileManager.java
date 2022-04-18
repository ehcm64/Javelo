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
        String zlString = Integer.toString(tileId.zoomLevel);
        String xString = Integer.toString(tileId.xIndex);
        String yFileString = tileId.yIndex + ".png";
        Path zlDir = cachePath.resolve(zlString);
        Path xDir = zlDir.resolve(xString);
        Path filePath = xDir.resolve(yFileString);
        if (Files.exists(filePath)) {
            try (InputStream input = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
                System.out.println("OUI");
                return new Image(input);
            }
        }
        URL url = new URL("https://" + this.tileServerName + "/" + zlString + "/" + xString + "/" + yFileString);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "JaVelo");
        try (InputStream i = new BufferedInputStream(connection.getInputStream())) {
            Files.createDirectories(xDir);
            Files.createFile(filePath);
            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                i.transferTo(outputStream);
                try (InputStream j = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
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
                    (0 <= point.y() && point.y() <= 1)
                    &&
                    zoomLevel <= 20;
        }
    }
}
