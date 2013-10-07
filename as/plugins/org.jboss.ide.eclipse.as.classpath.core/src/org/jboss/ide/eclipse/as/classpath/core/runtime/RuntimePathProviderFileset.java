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
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;

/**
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

}