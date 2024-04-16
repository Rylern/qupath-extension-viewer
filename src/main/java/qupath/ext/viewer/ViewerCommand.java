package qupath.ext.viewer;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.images.servers.ImageServers;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ViewerCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ViewerCommand.class);
    private final Stage owner;

    public ViewerCommand(Stage owner) {
        this.owner = owner;
    }

    @Override
    public void run() {
        try {
            //new Viewer(owner, ImageServers.buildServer("/Users/lleplat/QuPath/Images/CMU-1.tiff"));
            //new Viewer(owner, ImageServers.buildServer("/Users/lleplat/QuPath/Images/CMU-1.jpg"));
            //new Viewer(owner, ImageServers.buildServer("/Users/lleplat/QuPath/Images/mitosis.tif"));
            new Viewer(owner, new SampleImageServer(BufferedImage.class));
        } catch (IOException e) {
            logger.error("Error when creating the viewer", e);
        }
    }

    public static String getMenuTitle() {
        return "3D viewer";
    }
}
