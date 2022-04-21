package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
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
    private final int CACHE_SIZE = 100;

    public TileManager(Path cachePath, String tileServerName) {
        this.memoryCache = new LinkedHashMap<>(CACHE_SIZE, 0.75F, true);
        this.cachePath = cachePath;
        this.tileServerName = tileServerName;
    }

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
                return new Image(input);
            }
        }

        String urlFile = "/" + zlString + "/" + xString + "/" + yFileString;
        URL url = new URL("https", this.tileServerName, urlFile);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "JaVelo");

        try (InputStream i = new BufferedInputStream(connection.getInputStream())) {
            Files.createDirectories(xDirectory);
            Files.createFile(filePath);

            i.mark(32 * 1024); // 32kB read limit >>>> ~ 6kB image size
            Image tileImage = new Image(i);
            i.reset();

            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                i.transferTo(outputStream);
            }

            if (memoryCache.size() == CACHE_SIZE) {
                memoryCache.remove(memoryCache.keySet().iterator().next());
                memoryCache.put(tileId, tileImage);
            }
            return tileImage;
        }
    }

    record TileId(int zoomLevel, int xIndex, int yIndex) {

        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            return zoomLevel <= 20
                    && 0 <= xIndex && xIndex < Math.scalb(1, zoomLevel)
                    && 0 <= yIndex && yIndex < Math.scalb(1, zoomLevel);
        }
    }
}
