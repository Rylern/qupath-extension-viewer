package qupath.ext.viewer.scene;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Slicer extends Rectangle {

    public Slicer(
            int width,
            int height,
            int depth,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        super(-width, -height, 2*width, 2*height);
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
