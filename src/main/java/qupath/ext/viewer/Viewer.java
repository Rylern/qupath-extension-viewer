package qupath.ext.viewer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import qupath.ext.viewer.scene.Scene3D;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * The main window of the viewer. It contains 3 sliders to change the slider's
 * translation on the z-axis, rotation on the x-axis, and rotation on the y-axis.
 */
public class Viewer extends Stage {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.viewer.strings");
    @FXML
    private BorderPane root;
    @FXML
    private Slider translationSlider;
    @FXML
    private Slider xRotationSlider;
    @FXML
    private Slider yRotationSlider;

    /**
     * Create the viewer window.
     *
     * @param owner  the owner of this window
     * @param imageServer  the image to open in the viewer
     * @throws IOException when an exception occurs while creating the window
     */
    public Viewer(Stage owner, ImageServer<BufferedImage> imageServer) throws IOException {
        initUI(owner);

        translationSlider.setMax(Math.max(Math.max(imageServer.getWidth(), imageServer.getHeight()), imageServer.nZSlices()));
        root.setCenter(new Scene3D(
                getScene().widthProperty(),
                getScene().heightProperty(),
                imageServer,
                translationSlider.valueProperty(),
                xRotationSlider.valueProperty(),
                yRotationSlider.valueProperty()
        ).getSubScene());
    }

    private void initUI(Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(Viewer.class.getResource("viewer.fxml"), resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        setWidth(800);
        setHeight(600);

        if (owner != null) {
            initOwner(owner);
        }
        show();
    }
}
