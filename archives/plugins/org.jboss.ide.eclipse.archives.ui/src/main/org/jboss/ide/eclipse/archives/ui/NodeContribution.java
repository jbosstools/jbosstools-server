package org.jboss.ide.eclipse.archives.ui;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.jboss.ide.eclipse.archives.ui.actions.INodeActionDelegate;
import org.osgi.framework.Bundle;

public class NodeContribution implements Comparable {
	private String id, label;
	private INodeActionDelegate actionDelegate;
	private ImageDescriptor icon;
	private int weight;

	public NodeContribution (IConfigurationElement element) {
		id = element.getAttribute("id");
		label = element.getAttribute("label");
		
		try {
			actionDelegate = (INodeActionDelegate) element.createExecutableExtension("class");
		} catch (CoreException e) {
			//TODO			Trace.trace(getClass(), e);
		}
		
		String iconPath = element.getAttribute("icon");
		String pluginId = element.getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(pluginId);
		URL iconURL = iconPath == null ? null : FileLocator.find(bundle, new Path(iconPath), null);
		if (iconURL != null) {
			iconURL = bundle.getEntry(iconPath);
			icon = ImageDescriptor.createFromURL(iconURL);
		}
		String weightString = element.getAttribute("weight");
		weight = Integer.parseInt(weightString == null ? "100" : weightString);
	}
	
	public int compareTo(Object o) {
		if (o instanceof NodeContribution) {
			NodeContribution other = (NodeContribution) o;
			if (weight < other.getWeight()) return -1;
			else if (weight > other.getWeight()) return 1;
			else if (weight == other.getWeight()) {
				return label.compareTo(other.getLabel());
			}
		}
		return -1;
	}

	
	public INodeActionDelegate getActionDelegate() {
		return actionDelegate;
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public int getWeight() {
		return weight;
	}
}
