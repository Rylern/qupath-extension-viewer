package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageServerExtension {

    public static BufferedImage toRGB(BufferedImage image) {
        if (image == null) {
            return null;
        }

        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        } else {
            int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.setRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
            return rgbImage;
        }
    }

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

    public static BufferedImage getArea(ImageServer<BufferedImage> server, List<Point3D> points, int t) throws IOException {
        return null;
    }
}
