/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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

/**
 * An extended property provider that allows our xpath and 
 * fileset enhancements but denies all other functionality.
 */
public class EclipseServerExtendedProperties extends ServerExtendedProperties {
	public EclipseServerExtendedProperties(IAdaptable adaptable) {
		super(adaptable);
	}

	@Override
	public String getNewFilesetDefaultRootFolder() {
		return runtime.getLocation().toOSString();
	}
}
