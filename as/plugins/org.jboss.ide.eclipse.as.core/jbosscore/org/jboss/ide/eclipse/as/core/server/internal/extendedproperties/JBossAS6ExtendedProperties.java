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

import org.eclipse.core.runtime.IAdaptable;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.JBoss6ModuleStateVerifier;

public class JBossAS6ExtendedProperties extends JBossExtendedProperties {

	public JBossAS6ExtendedProperties(IAdaptable adaptable) {
		super(adaptable);
	}
	
	public IServerModuleStateVerifier getModuleStateVerifier() {
		return new JBoss6ModuleStateVerifier();
	}
}
