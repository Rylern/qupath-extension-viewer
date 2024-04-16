package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import qupath.ext.viewer.maths.Maths;

public class TestMaths {

    @Test
    void Check_Intersection_Between_Rectangles() {
        Maths.Rectangle rectangleA = new Maths.Rectangle(
                new Point3D(0, 0, 0),
                new Point3D(1, 0, 0),
                new Point3D(1, 2, 0)
        );
        Maths.Rectangle rectangleB = new Maths.Rectangle(
                new Point3D(0.5, -1, 2),
                new Point3D(0, 0, -5),      // not updated with new rectangle constructor
                new Point3D(0,5, 0)
        );
        Maths.Segment expectedSegment = new Maths.Segment(
                new Point3D(0.5, 0, 0),
                new Point3D(0.5, 2, 0)
        );

        Maths.Segment segment = Maths.findIntersectionLineBetweenRectangles(rectangleA, rectangleB);

        Assertions.assertEquals(expectedSegment, segment);
    }
}
