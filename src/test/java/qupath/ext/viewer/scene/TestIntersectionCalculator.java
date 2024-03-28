package qupath.ext.viewer.scene;

import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;

public class TestIntersectionCalculator {
    @Test
    void Check() {
        double width = 1;
        double height = 2;
        double depth = 1;
        Box box = new Box(width, height, depth);
        Rectangle rectangle = new Rectangle(-width, -height, 2*width, 2*height);
        //rectangle.getTransforms().add(new Rotate(60, Rotate.X_AXIS));

        /*
        TriangleMesh mesh = (TriangleMesh) IntersectionCalculator.getIntersectionMeshBetweenBoxAndRectangle(box, rectangle);

        System.err.println(mesh.getPoints());
        System.err.println(mesh.getTexCoords());
        System.err.println(mesh.getFaces());

         */
    }
}
