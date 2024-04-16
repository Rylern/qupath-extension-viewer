package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;
import qupath.ext.viewer.SampleImageServer;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class TestTemp {

    @Test
    void Check() {
        var imageServer = new SampleImageServer(BufferedImage.class);
        Function<Point3D, Point3D> t = p -> new Point3D(
                (p.getX() + imageServer.getPixelCalibration().getPixelWidth().doubleValue() * imageServer.getWidth() / 2) * (imageServer.getWidth() - 1) /
                        (imageServer.getPixelCalibration().getPixelWidth().doubleValue() * imageServer.getWidth()),
                (p.getY() - imageServer.getPixelCalibration().getPixelHeight().doubleValue() * imageServer.getHeight() / 2) * (imageServer.getHeight() - 1) /
                        (-imageServer.getPixelCalibration().getPixelHeight().doubleValue() * imageServer.getHeight()),
                (p.getZ() - imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices() / 2) * (imageServer.nZSlices() - 1) /
                        (-imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices())
        );

        System.err.println();
        System.err.println(t.apply(new Point3D(0, 0,-imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices() / 2)));
        System.err.println(t.apply(new Point3D(0, 0, 0)));
        System.err.println(t.apply(new Point3D(0, 0, imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices() / 2)));
        System.err.println();


    }
}
