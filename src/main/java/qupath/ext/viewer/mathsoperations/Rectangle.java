package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import qupath.ext.viewer.extensions.Point3DExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * <p>
 *     A rectangle in the 3D space.
 * </p>
 * <p>
 *     p0 represents the lower left corner the rectangle when viewed from a particular
 *     direction. This doesn't always mean it's the rectangle vertex with the lowest x and y components,
 *     the viewing direction can be different from (0, 0, 1). The only thing you can assume is that
 *     p2 comes after p1 which comes after p0 when the points are considered in the clockwise or
 *     anticlockwise direction.
 * </p>
 *
 * @param p0  the lower left corner of the rectangle
 * @param p1  the lower right corner of the rectangle
 * @param p2  the upper right corner of the rectangle
 */
public record Rectangle(Point3D p0, Point3D p1, Point3D p2) {

    /**
     * Create a copy of a rectangle and apply a transformation to its coordinates.
     *
     * @param rectangle the rectangle to copy
     * @param transform  the transformation to apply to the rectangle coordinates
     */
    public Rectangle(Rectangle rectangle, Function<Point3D, Point3D> transform) {
        this(
                transform.apply(rectangle.p0),
                transform.apply(rectangle.p1),
                transform.apply(rectangle.p2)
        );
    }

    /**
     * Create a rectangle from a JavaFX rectangle. This will the transforms of the JavaFX
     * rectangle into account.
     *
     * @param rectangle  the JavaFX rectangle to copy
     * @return a new rectangle corresponding to the JavaFX rectangle
     */
    public static Rectangle createFromJavaFXRectangle(javafx.scene.shape.Rectangle rectangle) {
        Point3D A = new Point3D(rectangle.getX(), rectangle.getY(), 0);
        Point3D B = new Point3D(rectangle.getX() + rectangle.getWidth(), rectangle.getY(), 0);
        Point3D C = new Point3D(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight(), 0);

        List<Transform> transforms = new ArrayList<>(rectangle.getTransforms());
        Collections.reverse(transforms);

        for (Transform transform: transforms) {
            A = transform.transform(A);
            B = transform.transform(B);
            C = transform.transform(C);
        }

        return new Rectangle(A, B, C);
    }

    /**
     * @return the upper left corner of the rectangle, as defined in {@link #Rectangle(Point3D,Point3D,Point3D)}.
     */
    public Point3D p3() {
        return p0.add(p2.subtract(p1));
    }

    /**
     * @return {@link #p1()} minus {@link #p0()}. It represents the
     * "width" of the rectangle
     */
    public Point3D getU() {
        return p1.subtract(p0);
    }

    /**
     * @return {@link #p2()} minus {@link #p1()}. It represents the
     * "height" of the rectangle
     */
    public Point3D getV() {
        return p2.subtract(p1);
    }

    /**
     * Compute the part of this rectangle that is inside a cube
     * (described by a list of rectangle that represents its faces).
     * The result is a list of points that describe a polygon.
     *
     * @param sidesOfCube the sides of the cube
     * @return the polygon describing the part of this rectangle that is located
     * inside the cube, or an empty list if such polygon doesn't exit
     */
    public List<Point3D> getPartOfRectangleInsideCube(List<Rectangle> sidesOfCube) {
        return sidesOfCube.stream()
                .map(side -> IntersectionCalculator.findIntersectionLineBetweenRectangles(side, this))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.a(), s.b()))
                .map(p -> Point3DExtension.round(p, 5))  // rounding is necessary to remove points not equal due to precision errors
                .distinct()
                .toList();
    }

    /**
     * Compute the part of this rectangle that is located in front of
     * another rectangle. The result is a list of points that describe a polygon.
     *
     * @param otherRectangle  the other rectangle
     * @return the polygon describing the part of this rectangle that is located
     * in front of the other rectangle, or an empty list if such polygon doesn't exit
     */
    public List<Point3D> getPartOfRectangleInFrontOfOtherRectangle(Rectangle otherRectangle) {
        List<Point3D> pointsOfRectangleInFrontOfPlane = Stream.of(
                p0,
                p1,
                p2,
                p3()
        )
                .filter(p -> Plane.createFromRectangle(otherRectangle).distanceOfPoint(p) > 0)
                .toList();

        Segment segment = IntersectionCalculator.findIntersectionLineBetweenRectangles(this, otherRectangle);

        if (segment == null) {
            return pointsOfRectangleInFrontOfPlane;
        } else {
            return Stream.concat(
                    pointsOfRectangleInFrontOfPlane.stream(),
                    Stream.of(segment.a(), segment.b())
            ).toList();
        }
    }
}
