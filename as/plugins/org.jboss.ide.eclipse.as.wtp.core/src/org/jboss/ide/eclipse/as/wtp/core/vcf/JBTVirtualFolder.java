/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;

/**
 * Currently does nothing at all, which is awesome.
 * Virtual components in jbt should be super dumb.
 * Stick with the model damnit. 
 * @author rob
 *
 */
public class JBTVirtualFolder extends VirtualFolder {
	private JBTVirtualComponent component;
	public JBTVirtualFolder(IProject aComponentProject, 
			IPath aRuntimePath, JBTVirtualComponent component) {
		super(aComponentProject, aRuntimePath);
		this.component = component;
	}
}
