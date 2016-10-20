/*******************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;
import org.jboss.tools.foundation.core.xml.XMLMemento;

/**
 * This is a default implementation of a simple fileset,
 * which includes a root folder, patterns for includes
 * and excludes, and the ability to resolve variables
 * in the root folder string, such as ${jboss_server_home}
 * 
 * @since 3.0
 */
public class RuntimePathProviderFileset extends Fileset implements IRuntimePathProvider {
	
	public RuntimePathProviderFileset(Fileset set) {
		super(set.getName(), set.getRawFolder(), set.getIncludesPattern(), set.getExcludesPattern());
	}
	public RuntimePathProviderFileset(String baseFolder) {
		this("", baseFolder, "**/*.jar", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	public RuntimePathProviderFileset(String name, String folder, String inc, String exc) {
		super(name, folder, inc, exc);
	}
	public IPath[] getAbsolutePaths() {
		IPath[] setPaths = findPaths();
		IPath[] absolute = new IPath[setPaths.length];
		for( int j = 0; j < setPaths.length; j++ ) {
			absolute[j] = new Path(getFolder()).append(setPaths[j]);
		}
		return absolute;
	}
	
	@Override
	public void setVariableResolver(IVariableResolver resolver) {
		super.setVariableResolver(resolver);
	}
	
	@Override
	public String getDisplayString() {
		return getRawFolder() + " - [" + getIncludesPattern() + "] - [" + getExcludesPattern() + "]";
	}
	
	@Override
	public void saveInMemento(XMLMemento memento) {
		XMLMemento child = (XMLMemento)memento.createChild("fileset");//$NON-NLS-1$
		child.putString("name", getName());//$NON-NLS-1$
		child.putString("folder", getRawFolder());//$NON-NLS-1$
		child.putString("includes", getIncludesPattern());//$NON-NLS-1$
		child.putString("excludes", getExcludesPattern());//$NON-NLS-1$
	}

}