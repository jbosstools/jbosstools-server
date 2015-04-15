/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
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
	
	

	private IRuntimePathProvider[] getDefaultJBossModulesEntries(IRuntimeType rtt) {
		if( rtt.getId().equals(IJBossToolingConstants.WILDFLY_80)) {
			return getDefaultJEE7JBossModulesEntries();
		}
		if( rtt.getId().equals(IJBossToolingConstants.WILDFLY_90)) {
			return getDefaultJEE8JBossModulesEntries();
		}
		
		return getDefaultJBossModulesEntries();
	}
	

	private IRuntimePathProvider[] getDefaultJEE8JBossModulesEntries() {
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

		// Convert
		String[] allString = (String[]) all.toArray(new String[all.size()]);
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
			"javax.activation.api",
			"javax.annotation.api",
			"javax.ejb.api",
			"javax.el.api",
			"javax.enterprise.api",
			"javax.enterprise.deploy.api",
			"javax.faces.api",
			"javax.inject.api",
			"javax.interceptor.api",
			"javax.jms.api",
			"javax.jws.api",
			"javax.mail.api",
			"javax.management.j2ee.api",
			"javax.persistence.api",
			"javax.resource.api",
			"javax.rmi.api",
			"javax.security.auth.message.api",
			"javax.security.jacc.api",
			"javax.servlet.api",
			"javax.servlet.jsp.api",
			"javax.servlet.jstl.api",
			"javax.transaction.api",
			"javax.validation.api",
			"javax.ws.rs.api",
			"javax.wsdl4j.api",
			"javax.xml.bind.api",
			"javax.xml.registry.api",
			"javax.xml.rpc.api",
			"javax.xml.soap.api",
			"javax.xml.ws.api",
			"org.hibernate.validator",
			"org.picketbox",
			"org.jboss.as.controller-client",
			"org.jboss.dmr",
			"org.jboss.logging",
			"org.jboss.resteasy.resteasy-jaxb-provider",
			"org.jboss.resteasy.resteasy-jaxrs",
			"org.jboss.resteasy.resteasy-multipart-provider",
			"org.jboss.ejb3"
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
