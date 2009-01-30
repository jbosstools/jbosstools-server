/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.model.types;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class AbstractArchiveType implements IArchiveType, IExecutableExtension {

	private IConfigurationElement element;
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if( element == null ) element = config;
	}
	public String getId() {
		return element.getAttribute("id"); //$NON-NLS-1$
	}

	public String getLabel() {
		return element.getAttribute("label"); //$NON-NLS-1$
	}
}
