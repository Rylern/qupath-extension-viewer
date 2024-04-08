package qupath.ext.viewer.scene;

import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageServerExtension {

    public static BufferedImage getFixedY(ImageServer<BufferedImage> server, int x, int z, int width, int depth, int y, int t) throws IOException {
        List<BufferedImage> slices = new ArrayList<>();

        for (int zSlice = z; zSlice < z+depth; zSlice++) {
            slices.add(server.readRegion(RegionRequest.createInstance(
                    server.getPath(),
                    1.0,
                    x,
                    y,
                    width,
                    1,
                    zSlice,
                    t
            )));
        }

        if (slices.get(0) == null) {
            return null;
        } else {
            BufferedImage image = new BufferedImage(width, depth, slices.get(0).getType());
            Graphics2D graphics = image.createGraphics();
            for (int i=0; i<slices.size(); i++) {
                graphics.drawImage(slices.get(i), null, 0, i);
            }
            graphics.dispose();
            return image;
        }
    }

    public static BufferedImage getFixedX(ImageServer<BufferedImage> server, int y, int z, int height, int depth, int x, int t) throws IOException {
        List<BufferedImage> slices = new ArrayList<>();


        for (int zSlice = z; zSlice < z+depth; zSlice++) {
            slices.add(server.readRegion(RegionRequest.createInstance(
                    server.getPath(),
                    1.0,
                    x,
                    y,
                    1,
                    height,
                    zSlice,
                    t
            )));
        }

        if (slices.get(0) == null) {
            return null;
        } else {
            BufferedImage image = new BufferedImage(depth, height, slices.get(0).getType());
            Graphics2D graphics = image.createGraphics();
            for (int i=0; i<slices.size(); i++) {
                graphics.drawImage(slices.get(i), null, i, 0);
            }
            graphics.dispose();
            return image;
        }
    }
}
