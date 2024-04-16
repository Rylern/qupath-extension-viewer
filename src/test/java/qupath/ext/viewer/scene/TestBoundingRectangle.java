package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import qupath.ext.viewer.maths.BoundingRectangle;
import qupath.ext.viewer.maths.Maths;

import java.util.List;

public class TestBoundingRectangle {

    @Test
    void Check() {
        List<Point3D> points = List.of(
                new Point3D(1, 2, 0),
                new Point3D(4, 0, 0),
                new Point3D(5, 2, 0),
                new Point3D(6, 4, 0),
                new Point3D(3, 10, 0),
                new Point3D(0, 4, 0)
        );
        Maths.Rectangle expectedRectangle = new Maths.Rectangle(new Point3D(0, 0,0), new Point3D(0, 0, 0), new Point3D(0, 0, 0));

        Maths.Rectangle boundingRectangle = BoundingRectangle.getBoundingRectangle(points);

        Assertions.assertEquals(expectedRectangle, boundingRectangle);
    }
}
