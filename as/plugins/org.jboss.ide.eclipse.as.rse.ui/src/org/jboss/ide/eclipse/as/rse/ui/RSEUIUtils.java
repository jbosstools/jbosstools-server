package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;

public class RSEUIUtils {
	public static IHost findHost(String name, IHost[] hosts) {
		if( hosts == null )
			hosts = RSECorePlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationCategory("files");
		for( int i = 0; i < hosts.length; i++ ) {
			if( hosts[i].getAliasName().equals(name))
				return hosts[i];
		}
		return null;
	}
}
