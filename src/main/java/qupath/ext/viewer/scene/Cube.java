package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A 3D cube representing a 3D image.
 * It is centered at (0, 0, 0) and its dimension are
 * (imageWidth * pixelWidth, imageHeight * pixelHeight, imageDepth * zSpacing).
 * It is possible to retrieve the part of the cube that is located in front of a rectangle.
 */
class Cube {

    private final List<Rectangle> sides;
    private final Function<Point3D, Point3D> spaceToPixelTransform;

    /**
     * Create the cube.
     *
     * @param imageServer  the image that the cube should represent
     */
    public Cube(ImageServer<?> imageServer) {
        sides = getSides(imageServer);
        spaceToPixelTransform = getSpaceToPixelTransform(imageServer);
    }

    /**
     * Compute the part of the cube that is located in front of a rectangle.
     * The result is a closed 3D shape, represented as a list of faces.
     *
     * @param rectangle  the rectangle the cube should be in front of
     * @return a list of faces that represent the part of the cube that is located in front of the rectangle
     */
    public List<Polygon> getPartOfCubeInFrontOfRectangle(Rectangle rectangle) {
        List<Polygon> polygons = new ArrayList<>();

        for (Rectangle side: this.sides) {
            List<Point3D> points = side.getPartOfRectangleInFrontOfOtherRectangle(rectangle);

            if (points.size() > 2) {
                polygons.add(new Polygon(points, spaceToPixelTransform));
            }
        }

        List<Point3D> pointsOfSlicer = rectangle.getPartOfRectangleInsideCube(this.sides);
        if (pointsOfSlicer.size() > 2) {
            polygons.add(new Polygon(pointsOfSlicer, spaceToPixelTransform));
        }

        return polygons;
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

    /**
     * Return a function that maps a point in space to a pixel coordinate of the image.
     *
     * @param imageServer  the image to get the pixel coordinates from
     * @return a function that maps a point in space to a pixel coordinate of the image
     */
    private static Function<Point3D, Point3D> getSpaceToPixelTransform(ImageServer<?> imageServer) {
        // The formula to map a value from a range to another range is:
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
