package qupath.ext.viewer.scene;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * A rectangle in a 3D space whose translation on the z-axis
 * and rotations and the x and y-axis can be updated.
 * Its is centered in (0, 0, 0).
 */
class Slicer extends Rectangle {

    /**
     * Create the slicer.
     *
     * @param width  the width of the slicer
     * @param height  the height of the slicer
     * @param depth  the depth of the image this slicer will slice
     * @param translationSliderValue  the translation on the z-axis this slider should have
     * @param xRotationSliderValue  the rotation on the x-axis this slider should have
     * @param yRotationSliderValue  the rotation on the y-axis this slider should have
     */
    public Slicer(
            double width,
            double height,
            double depth,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        super(-width/2, -height/2, width, height);
        setFill(Color.BLUE);

        updateTransforms(translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth);
        translationSliderValue.addListener((p, o, n) -> updateTransforms(translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));
        xRotationSliderValue.addListener((p, o, n) -> updateTransforms(translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));
        yRotationSliderValue.addListener((p, o, n) -> updateTransforms(translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));
    }

    private void updateTransforms(
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue,
            double depth
    ) {
        getTransforms().setAll(
                new Rotate(yRotationSliderValue.get(), Rotate.Y_AXIS),
                new Rotate(xRotationSliderValue.get(), Rotate.X_AXIS),
                new Translate(0, 0, translationSliderValue.get() - depth/2)
        );
    }
}
