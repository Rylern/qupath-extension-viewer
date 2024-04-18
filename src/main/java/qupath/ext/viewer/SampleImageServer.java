package qupath.ext.viewer;

import qupath.lib.images.servers.AbstractImageServer;
import qupath.lib.images.servers.ImageServerBuilder;
import qupath.lib.images.servers.ImageServerMetadata;
import qupath.lib.images.servers.PixelType;
import qupath.lib.regions.RegionRequest;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Collection;

/**
 * An image of size (x: 800, y: 600, z: 100, t:1) whose colour comes
 * from the pixel position.
 */
public class SampleImageServer extends AbstractImageServer<BufferedImage> {

    public SampleImageServer(Class<BufferedImage> imageClass) {
        super(imageClass);
    }

    @Override
    protected ImageServerBuilder.ServerBuilder<BufferedImage> createServerBuilder() {
        return null;
    }

    @Override
    protected String createID() {
        return null;
    }

    @Override
    public Collection<URI> getURIs() {
        return null;
    }

    @Override
    public String getServerType() {
        return null;
    }

    @Override
    public ImageServerMetadata getOriginalMetadata() {
        return new ImageServerMetadata.Builder()
                .width(800)
                .height(600)
                .pixelType(PixelType.INT8)
                .sizeT(1)
                .sizeZ(100)
                .build();
    }

    @Override
    public BufferedImage readRegion(RegionRequest request) {
        BufferedImage image = new BufferedImage(request.getWidth(), request.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int i=0; i<request.getWidth(); i++) {
            for (int j=0; j<request.getHeight(); j++) {
                image.setRGB(i, j, new Color(
                        ((float) i + request.getX()) / getWidth(),
                        ((float) j + request.getY()) / getHeight(),
                        ((float) request.getZ()) / nZSlices()
                ).getRGB());
            }
        }

        return image;
    }
}
