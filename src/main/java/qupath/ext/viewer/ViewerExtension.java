package qupath.ext.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;

public class ViewerExtension implements QuPathExtension, GitHubProject {
	
	private static final Logger logger = LoggerFactory.getLogger(ViewerExtension.class);
	private static final String EXTENSION_NAME = "Viewer extension";
	private static final String EXTENSION_DESCRIPTION = "A 3D viewer for QuPath";
	private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.5.1");
	private static final GitHubRepo EXTENSION_REPOSITORY = GitHubRepo.create(EXTENSION_NAME, "rylern", "qupath-extension-viewer");
	private boolean isInstalled = false;

	@Override
	public void installExtension(QuPathGUI qupath) {
		if (isInstalled) {
			logger.debug("{} is already installed", getName());
			return;
		}
		isInstalled = true;
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
