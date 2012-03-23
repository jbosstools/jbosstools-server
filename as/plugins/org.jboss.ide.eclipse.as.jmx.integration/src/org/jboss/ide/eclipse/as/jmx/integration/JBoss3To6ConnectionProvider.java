/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public class JBoss3To6ConnectionProvider extends AbstractJBossJMXConnectionProvider{
	public static final String PROVIDER_ID = "org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider"; //$NON-NLS-1$
	
	private JMXClassLoaderRepository repository;
	public JBoss3To6ConnectionProvider() {
		super();
		repository = new JMXClassLoaderRepository();
		JBossJMXConnectionProviderModel.getDefault().registerProvider(ServerExtendedProperties.JMX_AS_3_TO_6_PROVIDER, this);
	}
	
	public String getName(IConnectionWrapper wrapper) {
		if( wrapper instanceof JBossServerConnection) {
			return ((JBossServerConnection)wrapper).getName();
		}
		return null;
	}

	protected boolean belongsHere(IServer server) {
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(server);
		int type = props == null ? -1 : props.getJMXProviderType();
		return type == JBossExtendedProperties.JMX_AS_3_TO_6_PROVIDER;
	}
	
	protected IConnectionWrapper createConnection(IServer server) {
		return new JBossServerConnection(server);
	}
	
	public String getId() {
		return PROVIDER_ID;
	}
	public boolean hasClassloaderRepository() {
		return true;
	}
	
	public JMXClassLoaderRepository getClassloaderRepository() {
		return repository;
	}

}
