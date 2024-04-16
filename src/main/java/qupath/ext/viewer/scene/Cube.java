package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Cube {

    private final List<Rectangle> sides;
    private final Function<Point3D, Point3D> spaceToPixelTransform;

    public Cube(ImageServer<?> imageServer) {
        sides = getSides(imageServer);
        spaceToPixelTransform = getSpaceToPixelTransform(imageServer);
    }

    public List<Face> getFacesInFrontOfRectangle(Rectangle rectangle) {
        List<Face> faces = new ArrayList<>();

        for (Rectangle side: this.sides) {
            List<Point3D> points = side.getPartOfRectangleInFrontOfOtherRectangle(rectangle);

            if (points.size() > 2) {
                faces.add(new Face(points, spaceToPixelTransform));
            }
        }

        List<Point3D> pointsOfSlicer = rectangle.getPartOfRectangleInsideCube(this.sides);
        if (pointsOfSlicer.size() > 2) {
            faces.add(new Face(pointsOfSlicer, spaceToPixelTransform));
        }

        return faces;
    }

    private static List<Rectangle> getSides(ImageServer<?> imageServer) {
        Point3D upperLeftClose = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
        );
        Point3D upperRightClose = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
        );
        Point3D lowerRightClose = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
        );
        Point3D lowerLeftClose = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
        );
        Point3D upperLeftAway = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
        );
        Point3D upperRightAway = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
        );
        Point3D lowerRightAway = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
        );
        Point3D lowerLeftAway = new Point3D(
                (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
        );

        return List.of(
                new Rectangle(
                        upperLeftClose,
                        upperRightClose,
                        lowerRightClose
                ),
                new Rectangle(
                        upperLeftClose,
                        upperRightClose,
                        upperRightAway
                ),
                new Rectangle(
                        lowerLeftClose,
                        lowerRightClose,
                        lowerRightAway
                ),
                new Rectangle(
                        upperLeftClose,
                        upperLeftAway,
                        lowerLeftAway
                ),
                new Rectangle(
                        upperRightClose,
                        upperRightAway,
                        lowerRightAway
                ),
                new Rectangle(
                        upperLeftAway,
                        upperRightAway,
                        lowerRightAway
                )
        );
    }

    private static Function<Point3D, Point3D> getSpaceToPixelTransform(ImageServer<?> imageServer) {
        // [c,d] = ((t-a)(d-c) / (b-a) + c) [a,b]

        return p -> new Point3D(
                (p.getX() + imageServer.getPixelCalibration().getPixelWidth().doubleValue() * imageServer.getWidth() / 2) * (imageServer.getWidth() - 1) /
                        (imageServer.getPixelCalibration().getPixelWidth().doubleValue() * imageServer.getWidth()),
                (p.getY() + imageServer.getPixelCalibration().getPixelHeight().doubleValue() * imageServer.getHeight() / 2) * (imageServer.getHeight() - 1) /
                        (imageServer.getPixelCalibration().getPixelHeight().doubleValue() * imageServer.getHeight()),
                (p.getZ() - imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices() / 2) * (imageServer.nZSlices() - 1) /
                        (-imageServer.getPixelCalibration().getZSpacing().doubleValue() * imageServer.nZSlices())
        );
    }
}
