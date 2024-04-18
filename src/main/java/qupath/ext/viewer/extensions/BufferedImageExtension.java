package qupath.ext.viewer.extensions;

import java.awt.image.BufferedImage;


/**
 * Provide some useful functions on {@link BufferedImage} objects.
 */
public class BufferedImageExtension {

    private BufferedImageExtension() {
        throw new AssertionError("This class is not instantiable.");
    }

    /**
     * Converts a BufferedImage of any type to an RGB BufferedImage.
     * If the provided image is already RGB, it is directly returned.
     *
     * @param image  the image to convert
     * @return the RGB equivalent of the provided image. Can be null if the
     * provided image is null.
     */
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
}
