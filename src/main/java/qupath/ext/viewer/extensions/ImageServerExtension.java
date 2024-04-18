package qupath.ext.viewer.extensions;

import javafx.geometry.Point3D;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Provide some useful functions on {@link ImageServer} objects.
 */
public class ImageServerExtension {

    private ImageServerExtension() {
        throw new AssertionError("This class is not instantiable.");
    }

    /**
     * Read a rectangular region of an ImageServer with all channels and at a particular time point.
     * The region can be located anywhere in the (x, y, z) space.
     * The width (height) of the returned image is the width (height) of the region + 1.
     * This function is very slow because it reads the region pixel by pixel.
     *
     * @param server  the image to read
     * @param area  the region of the image to read. It must be a rectangle, otherwise an unexpected result
     *              is returned
     * @param t  the time point to read
     * @return the portion of image corresponding to the provided region of the provided image server
     * @throws IOException when an exception occurs while reading the image
     */
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
