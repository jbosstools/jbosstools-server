package org.jboss.ide.eclipse.archives.ui;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.archives.ui.actions.NewArchiveAction;

public class ExtensionManager {
	public static final String NODE_POPUP_MENUS_EXTENSION_ID = "org.jboss.ide.eclipse.archives.ui.nodePopupMenus";
	public static final String NEW_PACKAGE_ACTIONS_EXTENSION_ID = "org.jboss.ide.eclipse.archives.ui.newArchiveActions";
	
	private NewArchiveAction[] newArchiveActions;
	private NodeContribution[] nodeContributions;
	public NewArchiveAction[] getNewArchiveActions() {
		if( newArchiveActions == null )
			newArchiveActions = findNewArchiveActions();
		return newArchiveActions;
	}
	public NodeContribution[] getNodeContributions() {
		if( nodeContributions == null )
			nodeContributions = findNodePopupMenuContributions();
		return nodeContributions;
	}
	
	public static IExtension[] findExtension (String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}
	
	public static NodeContribution[] findNodePopupMenuContributions () {
		ArrayList contributions = new ArrayList();
		IExtension[] extensions = findExtension(NODE_POPUP_MENUS_EXTENSION_ID);
		
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				contributions.add(new NodeContribution(elements[j]));
			}
		}
		
		return (NodeContribution[]) contributions.toArray(new NodeContribution[contributions.size()]);
	}
	
	public static NewArchiveAction[] findNewArchiveActions () {
		ArrayList contributions = new ArrayList();
		IExtension[] extensions = findExtension(NEW_PACKAGE_ACTIONS_EXTENSION_ID);
		
		for (int i = 0; i < extensions.length; i++)
		{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++)
			{
				contributions.add(new NewArchiveAction(elements[j]));
			}
		}
		
		return (NewArchiveAction[]) contributions.toArray(new NewArchiveAction[contributions.size()]);
	}
}
