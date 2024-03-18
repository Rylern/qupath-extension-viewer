package qupath.ext.viewer;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ViewerCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ViewerCommand.class);
    private final Stage owner;
    private Viewer viewer;

    public ViewerCommand(Stage owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        if (viewer == null) {
            try {
                viewer = new Viewer(owner);
            } catch (IOException e) {
                logger.error("Error when creating the viewer", e);
            }
        } else {
            viewer.show();
            viewer.requestFocus();
        }
    }

    public static String getMenuTitle() {
        return "3D viewer";
    }
}
