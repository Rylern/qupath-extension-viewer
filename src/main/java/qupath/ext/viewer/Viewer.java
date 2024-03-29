package qupath.ext.viewer;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import qupath.ext.viewer.scene.Scene3D;

import java.io.IOException;

public class Viewer extends Stage {

    @FXML
    private BorderPane root;
    @FXML
    private Slider translationSlider;
    @FXML
    private Slider xRotationSlider;
    @FXML
    private Slider yRotationSlider;

    public Viewer(Stage owner, int imageWidth, int imageHeight, int imageDepth) throws IOException {
        initUI(owner);

        translationSlider.setMax(Math.max(Math.max(imageWidth, imageHeight), imageDepth));
        root.setCenter(new Scene3D(
                getScene().widthProperty(),
                getScene().heightProperty(),
                imageWidth,
                imageHeight,
                imageDepth,
                translationSlider.valueProperty(),
                xRotationSlider.valueProperty(),
                yRotationSlider.valueProperty()
        ).getSubScene());
    }

    private void initUI(Stage owner) throws IOException {
        UiUtilities.loadFXML(this, Viewer.class.getResource("viewer.fxml"));

        setWidth(800);
        setHeight(600);

        if (owner != null) {
            initOwner(owner);
        }
        show();
    }
}
