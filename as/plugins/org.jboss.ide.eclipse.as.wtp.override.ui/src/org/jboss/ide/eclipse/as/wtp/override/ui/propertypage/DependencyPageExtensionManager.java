package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.jboss.ide.eclipse.as.wtp.override.ui.WTPOveridePlugin;

public class DependencyPageExtensionManager {
	private static DependencyPageExtensionManager manager = null;
	public static DependencyPageExtensionManager getManager() {
		if( manager == null )
			manager = new DependencyPageExtensionManager();
		return manager;
	}
	
	private HashMap<String, IDependencyPageProvider> providers = null;
	
	public IDependencyPageProvider getProvider(IFacetedProject project) {
		if( providers == null )
			loadProviders();
		Iterator<IDependencyPageProvider> i = providers.values().iterator();
		IDependencyPageProvider temp;
		while(i.hasNext()) {
			temp = i.next();
			if( temp.canHandle(project))
				return temp;
		}
		return null;
	}
	
	private void loadProviders() {
		HashMap<String, IDependencyPageProvider> temp = new HashMap<String, IDependencyPageProvider>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(
				WTPOveridePlugin.PLUGIN_ID, "moduleDependencyPropertyPage");
		for( int i = 0; i < cf.length; i++ ) {
			try {
				temp.put(cf[i].getAttribute("id"),  //$NON-NLS-1$
					(IDependencyPageProvider)cf[i].createExecutableExtension("class"));  //$NON-NLS-1$
			} catch( CoreException ce ) {}
		}
		providers = temp;
	}
	
}
