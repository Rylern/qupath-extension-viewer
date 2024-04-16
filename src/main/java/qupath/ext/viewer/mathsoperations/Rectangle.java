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
 * params in order
 *
 * @param p0
 * @param p1
 * @param p2
 */
public record Rectangle(Point3D p0, Point3D p1, Point3D p2) {

    public Rectangle(Rectangle rectangle, Function<Point3D, Point3D> transform) {
        this(
                transform.apply(rectangle.p0),
                transform.apply(rectangle.p1),
                transform.apply(rectangle.p2)
        );
    }

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

    public Point3D p3() {
        return p0.add(p2.subtract(p1));
    }

    public Point3D getU() {
        return p1.subtract(p0);
    }

    public Point3D getV() {
        return p2.subtract(p1);
    }

    public List<Point3D> getPartOfRectangleInsideCube(List<Rectangle> sidesOfCube) {
        return sidesOfCube.stream()
                .map(side -> IntersectionCalculator.findIntersectionLineBetweenRectangles(side, this))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.a(), s.b()))
                .map(p -> Point3DExtension.round(p, 5))  // rounding is necessary to remove points not equal due to precision errors
                .distinct()
                .toList();
    }

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
