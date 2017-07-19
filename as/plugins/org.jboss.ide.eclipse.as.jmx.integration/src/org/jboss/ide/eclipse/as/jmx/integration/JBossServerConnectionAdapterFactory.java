/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public class JBossServerConnectionAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if( adaptableObject instanceof JBossServer ) {
			JBossServer jbs = (JBossServer)adaptableObject;
			IServer is = jbs.getServer();
			IConnectionWrapper w = JBossJMXConnectionProviderModel.getDefault().getConnection(is);
			return adapterType.cast(w);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{IConnectionWrapper.class};
	}

}
