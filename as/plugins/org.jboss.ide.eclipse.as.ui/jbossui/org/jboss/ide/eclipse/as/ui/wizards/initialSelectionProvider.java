package org.jboss.ide.eclipse.as.ui.wizards;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.viewers.InitialSelectionProvider;

public class initialSelectionProvider extends InitialSelectionProvider {

	public initialSelectionProvider() {
	}
	
	public IServerType getInitialSelection(IServerType[] serverTypes) {
		if (serverTypes == null)
			return null;
		
		int size = serverTypes.length;
		for (int i = 0; i < size; i++) {
			if( serverTypes[i].getId().equals("org.jboss.ide.eclipse.as.40"))
				return serverTypes[i];
		}
		return serverTypes[0];
	}


}
