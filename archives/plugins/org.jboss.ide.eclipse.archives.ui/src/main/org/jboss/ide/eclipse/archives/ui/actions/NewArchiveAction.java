package org.jboss.ide.eclipse.archives.ui.actions;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IActionDelegate;
import org.osgi.framework.Bundle;

/**
 * This is an action wrapper. It wraps the extension point
 */
public class NewArchiveAction {

	private String id, label;
	private ImageDescriptor icon;
	private Image iconImage;
	private IActionDelegate action;
	
	public NewArchiveAction(IConfigurationElement element) {
		id = element.getAttribute("id");
		label = element.getAttribute("label");
		
		try {
			action = (IActionDelegate) element.createExecutableExtension("class");
		} catch (CoreException e) {
			//TODO			
		}
		
		String iconPath = element.getAttribute("icon");
		String pluginId = element.getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(pluginId);
		URL iconURL = FileLocator.find(bundle, new Path(iconPath), null);
		if (iconURL == null) {
			iconURL = bundle.getEntry(iconPath);
		}
		icon = ImageDescriptor.createFromURL(iconURL);
		iconImage = icon.createImage();
	}

	public IActionDelegate getAction() {
		return action;
	}

	public ImageDescriptor getIconDescriptor() {
		return icon;
	}
	
	public Image getIcon() {
		return iconImage;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	protected void finalize() throws Throwable {
		iconImage.dispose();
	}

}
