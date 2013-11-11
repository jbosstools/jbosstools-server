/*******************************************************************************
 * Copyright (c) 2011-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import org.eclipse.core.runtime.IPath;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;

/**
 * An object capable of returning paths
 * @since 3.0
 */
public interface IRuntimePathProvider {
	
	/**
	 * Set a way to resolve given variables that may be
	 * included as part of the underlying paths. 
	 */
	public void setVariableResolver(IVariableResolver resolver);

	
	/**
	 * Get an array of IPath objects representing local files.
	 * This method returns absolute file-system paths
	 * which have already had variable string replacement performed on them.
	 * @return
	 */
	public IPath[] getAbsolutePaths();
	
}