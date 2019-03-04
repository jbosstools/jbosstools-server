/*******************************************************************************
 * Copyright (c) 2014-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.jbossmodules.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.InternalRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

/**
 * This class is in charge of what default module jars 
 * should be returned for a given runtime type
 */
public class JBossModulesDefaultClasspathModel extends InternalRuntimeClasspathModel {
	
	public JBossModulesDefaultClasspathModel(IRuntimeType rtt) {
		super();
		setProviders(getDefaultJBossModulesEntries(rtt));
	}
	
	private static List<String> ee7 = Arrays.asList(new String[] {IJBossToolingConstants.WILDFLY_80});
	private static List<String> ee8 = Arrays.asList(new String[] {
			IJBossToolingConstants.WILDFLY_90, IJBossToolingConstants.WILDFLY_100,
			IJBossToolingConstants.EAP_70});
	private static List<String> wf11Plus = Arrays.asList(new String[] {
			IJBossToolingConstants.WILDFLY_110, IJBossToolingConstants.WILDFLY_120, IJBossToolingConstants.WILDFLY_130,
			IJBossToolingConstants.WILDFLY_140, IJBossToolingConstants.WILDFLY_150, IJBossToolingConstants.WILDFLY_160,
			IJBossToolingConstants.EAP_71, IJBossToolingConstants.EAP_72});
	// NEW_SERVER_ADAPTER

	private IRuntimePathProvider[] getDefaultJBossModulesEntries(IRuntimeType rtt) {
		String rttId = rtt.getId();
		if( ee7.contains(rttId))
			return getDefaultJEE7JBossModulesEntries();
		if( ee8.contains(rttId))
			return getDefaultJEE8JBossModulesEntries();
		if( wf11Plus.contains(rttId)) 
			return getDefaultWF11JBossModulesEntries();
		// NEW_SERVER_ADAPTER
		return getDefaultJBossModulesEntries();
	}



	
	private IRuntimePathProvider[] getDefaultWF11JBossModulesEntries() {
		ArrayList<String> all = new ArrayList<String>();
		all.addAll(Arrays.asList(getDefaultJEE8JBossModulesEntryKeys()));
		
		all.add("org.wildfly.common"); //$NON-NLS-1$
		all.add("org.wildfly.security.elytron-private"); //$NON-NLS-1$
		
		String[] allString = (String[]) all.toArray(new String[all.size()]);
		return toRuntimePathProvider(allString);
	}

	private String[] getDefaultJEE8JBossModulesEntryKeys() {
		ArrayList<String> all = new ArrayList<String>();
		all.addAll(Arrays.asList(getDefaultJBossModulesEntryKeys()));
		all.add("javax.batch.api"); //$NON-NLS-1$
		all.add("javax.enterprise.concurrent.api"); //$NON-NLS-1$
		all.add("javax.websocket.api"); //$NON-NLS-1$ 
		all.add("javax.json.api"); //$NON-NLS-1$ 
		
		// Modules removed since as7/wf8
		all.remove("javax.enterprise.deploy.api");//$NON-NLS-1$
		all.remove("javax.rmi.api");//$NON-NLS-1$
		all.remove("javax.xml.registry.api");//$NON-NLS-1$

		return (String[]) all.toArray(new String[all.size()]);
	}
	private IRuntimePathProvider[] getDefaultJEE8JBossModulesEntries() {
		String[] allString = getDefaultJEE8JBossModulesEntryKeys();
		return toRuntimePathProvider(allString);
	}
	
	private IRuntimePathProvider[] getDefaultJEE7JBossModulesEntries() {
		ArrayList<String> all = new ArrayList<String>();
		all.addAll(Arrays.asList(getDefaultJBossModulesEntryKeys()));
		all.add("javax.batch.api"); //$NON-NLS-1$
		all.add("javax.enterprise.concurrent.api"); //$NON-NLS-1$
		all.add("javax.websocket.api"); //$NON-NLS-1$ 
		all.add("javax.json.api"); //$NON-NLS-1$ 
		String[] allString = (String[]) all.toArray(new String[all.size()]);
		return toRuntimePathProvider(allString);
	}
	
	private String[] getDefaultJBossModulesEntryKeys() {
		String[] ret = new String[]{
			"javax.activation.api", //$NON-NLS-1$
			"javax.annotation.api",//$NON-NLS-1$
			"javax.ejb.api",//$NON-NLS-1$
			"javax.el.api",//$NON-NLS-1$
			"javax.enterprise.api",//$NON-NLS-1$
			"javax.enterprise.deploy.api",//$NON-NLS-1$
			"javax.faces.api",//$NON-NLS-1$
			"javax.inject.api",//$NON-NLS-1$
			"javax.interceptor.api",//$NON-NLS-1$
			"javax.jms.api",//$NON-NLS-1$
			"javax.jws.api",//$NON-NLS-1$
			"javax.mail.api",//$NON-NLS-1$
			"javax.management.j2ee.api",//$NON-NLS-1$
			"javax.persistence.api",//$NON-NLS-1$
			"javax.resource.api",//$NON-NLS-1$
			"javax.rmi.api",//$NON-NLS-1$
			"javax.security.auth.message.api",//$NON-NLS-1$
			"javax.security.jacc.api",//$NON-NLS-1$
			"javax.servlet.api",//$NON-NLS-1$
			"javax.servlet.jsp.api",//$NON-NLS-1$
			"javax.servlet.jstl.api",//$NON-NLS-1$
			"javax.transaction.api",//$NON-NLS-1$
			"javax.validation.api",//$NON-NLS-1$
			"javax.ws.rs.api",//$NON-NLS-1$
			"javax.wsdl4j.api",//$NON-NLS-1$
			"javax.xml.bind.api",//$NON-NLS-1$
			"javax.xml.registry.api",//$NON-NLS-1$
			"javax.xml.rpc.api",//$NON-NLS-1$
			"javax.xml.soap.api",//$NON-NLS-1$
			"javax.xml.ws.api",//$NON-NLS-1$
			"org.hibernate.validator",//$NON-NLS-1$
			"org.picketbox",//$NON-NLS-1$
			"org.jboss.as.controller-client",//$NON-NLS-1$
			"org.jboss.dmr",//$NON-NLS-1$
			"org.jboss.logging",//$NON-NLS-1$
			"org.jboss.resteasy.resteasy-jaxb-provider",//$NON-NLS-1$
			"org.jboss.resteasy.resteasy-jaxrs",//$NON-NLS-1$
			"org.jboss.resteasy.resteasy-multipart-provider",//$NON-NLS-1$
			"org.jboss.ejb3"//$NON-NLS-1$
		};
		return ret;
	}	
	
	private IRuntimePathProvider[] toRuntimePathProvider(String[] modules) {
		ArrayList<IRuntimePathProvider> sets = new ArrayList<IRuntimePathProvider>();
		for( int i = 0; i < modules.length; i++ ) {
			sets.add(createModulePath(modules[i]));
		}
		return (IRuntimePathProvider[]) sets.toArray(new IRuntimePathProvider[sets.size()]);
	}
	
	private IRuntimePathProvider[] getDefaultJBossModulesEntries() {
		return toRuntimePathProvider(getDefaultJBossModulesEntryKeys());
	}
	
	private LayeredProductPathProvider createModulePath(String moduleName) {
		return createModulePath(moduleName, null);
	}
	private LayeredProductPathProvider createModulePath(String moduleName, String slot) {
		return new LayeredProductPathProvider(moduleName, slot);
	}
	
}
