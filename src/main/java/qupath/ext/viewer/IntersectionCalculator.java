package qupath.ext.viewer;

import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;

import java.util.List;

public class IntersectionCalculator {

    public static List<Point3D> getIntersectionPointsBetweenBoxAndRectangle(Box box, Rectangle rectangle) {


        getRectanglesOfBox(box).stream()
                .map(r -> Maths.findIntersectionLineBetweenRectangles(
                        r,

                ))
    }

    private static Maths.Rectangle getRectangle(Rectangle rectangle) {
        Point3D A = new Point3D(rectangle.getX(), rectangle.getY(), 0);
        Point3D B = new Point3D(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight(), 0);

        for (Transform transform: rectangle.getTransforms()) {
            A = transform.transform(A);
            B = transform.transform(B);
        }

        return new Maths.Rectangle(
                A,

        )
    }

    private static List<Maths.Rectangle> getRectanglesOfBox(Box box) {
        Point3D center = new Point3D(0, 0, 0);
        Point3D width = new Point3D(box.getWidth(), 0, 0);
        Point3D height = new Point3D(0, box.getHeight(), 0);
        Point3D depth = new Point3D(0, 0, box.getDepth());

        for (Transform transform: box.getTransforms()) {
            center = transform.transform(center);
            width = transform.transform(width);
            height = transform.transform(height);
            depth = transform.transform(depth);
        }

        Point3D upperLeftClose = center.subtract(new Point3D(box.getWidth() / 2, box.getHeight() / 2, box.getDepth() / 2));
        Point3D lowerRightAway = center.add(new Point3D(box.getWidth() / 2, box.getHeight() / 2, box.getDepth() / 2));

        return List.of(
                new Maths.Rectangle(
                        upperLeftClose,
                        width,
                        depth
                ),
                new Maths.Rectangle(
                        upperLeftClose,
                        width,
                        height
                ),
                new Maths.Rectangle(
                        upperLeftClose,
                        depth,
                        height
                ),
                new Maths.Rectangle(
                        lowerRightAway,
                        width.multiply(-1),
                        depth.multiply(-1)
                ),
                new Maths.Rectangle(
                        lowerRightAway,
                        width.multiply(-1),
                        height.multiply(-1)
                ),
                new Maths.Rectangle(
                        lowerRightAway,
                        depth.multiply(-1),
                        height.multiply(-1)
                )
        );
    }
}
