package qupath.ext.viewer.extensions;

import javafx.geometry.Point3D;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageServerExtension {

    public static BufferedImage toRGB(BufferedImage image) {
        if (image == null) {
            return null;
        } else if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        } else {
            int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.setRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
            return rgbImage;
        }
    }

    public static BufferedImage readRegion(ImageServer<BufferedImage> server, Rectangle area, int t) throws IOException {
        int width = (int) area.getU().magnitude() + 1;
        int height = (int) area.getV().magnitude() + 1;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int j=0; j<height; j++) {
            Point3D rowBeginning = area.p0().interpolate(area.p3(), (double) j / height);
            Point3D rowEnding = area.p1().interpolate(area.p2(), (double) j / height);

            for (int i=0; i<width; i++) {
                Point3D point = rowBeginning.interpolate(rowEnding, (double) i / width);
                int x = (int) Math.round(point.getX());
                int y = (int) Math.round(point.getY());
                int z = (int) Math.round(point.getZ());

                if (0 <= x && x < server.getWidth() && 0 <= y && y < server.getHeight() && 0 <= z && z < server.nZSlices()) {
                    BufferedImage pixel = server.readRegion(RegionRequest.createInstance(
                            server.getPath(),
                            1.0,
                            (int) point.getX(),
                            (int) point.getY(),
                            1,
                            1,
                            (int) point.getZ(),
                            t
                    ));
                    image.setRGB(i, j, pixel.getRGB(0, 0));
                }
            }
        }

        return image;
    }
}
