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

import java.util.HashMap;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JBossJMXConnectionProviderModel {
	// Singleton
	private static JBossJMXConnectionProviderModel instance;
	public static JBossJMXConnectionProviderModel getDefault() {
		if( instance == null )
			instance = new JBossJMXConnectionProviderModel();
		return instance;
	}
	
	private HashMap<Integer, AbstractJBossJMXConnectionProvider> providers;
	public JBossJMXConnectionProviderModel() {
		providers = new HashMap<Integer, AbstractJBossJMXConnectionProvider>();
//		providers.put(ServerExtendedProperties.JMX_NULL_PROVIDER, null);
//		providers.put(ServerExtendedProperties.JMX_AS_3_TO_6_PROVIDER, new JBoss3To6ConnectionProvider());
//		providers.put(ServerExtendedProperties.JMX_DEFAULT_PROVIDER, new JBoss70ConnectionProvider());
//		providers.put(ServerExtendedProperties.JMX_AS_710_PROVIDER, null);		
	}
	
	public void registerProvider(int type, AbstractJBossJMXConnectionProvider provider) {
		providers.put(type, provider);
	}
	
	public AbstractJBossJMXConnectionProvider getProvider(int type) {
		return providers.get(type);
	}

	
	public IConnectionWrapper getConnection(IServer s) {
		AbstractJBossJMXConnectionProvider provider = getProvider(s);
		if( provider == null )
			return null;
		return provider.findConnection(s);
	}

	public AbstractJBossJMXConnectionProvider getProvider(IServer s) {
		ServerExtendedProperties properties = (ServerExtendedProperties) s.loadAdapter(ServerExtendedProperties.class, null);
		if( properties == null )
			return null;
		
		int i = properties.getJMXProviderType();
		AbstractJBossJMXConnectionProvider provider = providers.get(i);
		return provider;
	}
	
	// Run this action on the server. 
	// If the connection doesn't exist, make a new one
	public void run(IServer s, IJMXRunnable r) throws JMXException {
		IConnectionWrapper c = getConnection(s);
		if( c != null )
			// JMX is not installed here
			c.run(r);
	}

}
