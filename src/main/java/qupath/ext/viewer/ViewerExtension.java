package qupath.ext.viewer;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.actions.ActionTools;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Add two actions in the Extensions menu to open the viewer with the currently opened image
 * and with the sample image described in {@link SampleImageServer}.
 */
public class ViewerExtension implements QuPathExtension, GitHubProject {

	private static final String EXTENSION_NAME = "Viewer extension";
	private static final String EXTENSION_DESCRIPTION = "A 3D viewer for QuPath";
	private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.5.1");
	private static final GitHubRepo EXTENSION_REPOSITORY = GitHubRepo.create(EXTENSION_NAME, "rylern", "qupath-extension-viewer");
	private boolean isInstalled = false;

	@Override
	public void installExtension(QuPathGUI qupath) {
		if (!isInstalled) {
			if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
				throw new RuntimeException("3D is not available on this machine");
			}

			MenuTools.addMenuItems(qupath.getMenu("Extensions", false),
					MenuTools.createMenu("3D viewer",
							ActionTools.createAction(
									() -> {
										try {
											new Viewer(qupath.getStage(), new SampleImageServer(BufferedImage.class));
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									},
									"Open the sample image"
							),
							ActionTools.createAction(
									() -> {
										try {
											ImageServer<BufferedImage> server = QuPathGUI.getInstance().getViewer().getServer();
											if (server != null) {
												new Viewer(qupath.getStage(), server);
											}
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									},
									"Open the current image"
							)
					)
			);

			isInstalled = true;
		}
	}

	@Override
	public String getName() {
		return EXTENSION_NAME;
	}

	@Override
	public String getDescription() {
		return EXTENSION_DESCRIPTION;
	}
	
	@Override
	public Version getQuPathVersion() {
		return EXTENSION_QUPATH_VERSION;
	}

	@Override
	public GitHubRepo getRepository() {
		return EXTENSION_REPOSITORY;
	}
}
