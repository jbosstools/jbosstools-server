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
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;

/**
 * This class is the entry point when WTP is attempting to adapt a 
 * IRuntimeComponent into an IClasspathProvider.  This is the first step
 * in turning a project / facet / runtime combination into classpath entries
 * that should be added to a project.  
 */
public final class JBossRuntimeClasspathProviderAdapterFactory implements IAdapterFactory {
	private static final Class[] ADAPTER_TYPES = { IClasspathProvider.class };

	public Object getAdapter(final Object adaptable, final Class adapterType) {
		IRuntimeComponent rc = (IRuntimeComponent) adaptable;
		return new ProjectRuntimeClasspathProvider(rc);
	}

	public Class[] getAdapterList() {
		return ADAPTER_TYPES;
	}
}