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
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import org.eclipse.core.runtime.IPath;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;
import org.jboss.tools.foundation.core.xml.XMLMemento;

/**
 * An object capable of returning paths
 * @since 3.0
 */
public interface IRuntimePathProvider {
	
	/**
	 * Set a way to resolve given variables that may be
	 * included as part of the underlying paths. 
	 * For example, base directories for servers such as 
	 * AS 5 / AS 6 may use variables like ${jboss_config_location},
	 * which need to be resolved against the actual runtime. 
	 */
	public void setVariableResolver(IVariableResolver resolver);

	
	/**
	 * Get an array of IPath objects representing local files.
	 * This method returns absolute file-system paths
	 * which have already had variable string replacement performed on them.
	 * @return
	 */
	public IPath[] getAbsolutePaths();
	
	
	/**
	 * Get a string representation for the purposes
	 * of display
	 * @return
	 */
	public String getDisplayString();
	
	/**
	 * Persist this provider in the given xml memento.
	 * 
	 * @param memento
	 */
	public void saveInMemento(XMLMemento memento);
	
}