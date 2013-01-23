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
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SPACE;

import org.eclipse.core.runtime.IAdaptable;
import org.jboss.ide.eclipse.as.core.Messages;

/**
 *
 */
public class JBossAS710ExtendedProperties extends JBossAS7ExtendedProperties {

	public JBossAS710ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "7.x"; //$NON-NLS-1$
	}

	public int getJMXProviderType() {
		return JMX_OVER_AS_MANAGEMENT_PORT_PROVIDER;
	}
	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}
}
