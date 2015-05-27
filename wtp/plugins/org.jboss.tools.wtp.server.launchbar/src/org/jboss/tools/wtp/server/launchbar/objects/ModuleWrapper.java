/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar.objects;

import org.eclipse.wst.server.core.IModule;

/**
 * An object, to be returned by an ObjectProvider,
 * which represents an existing workspace module
 */
public class ModuleWrapper {
	private IModule module;
	public ModuleWrapper(IModule m) {
		this.module = m;
	}
	
	public IModule getModule() {
		return module;
	}
}