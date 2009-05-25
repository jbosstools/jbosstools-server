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

package org.jboss.ide.eclipse.as.classpath.core.jee;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
import org.jboss.ide.eclipse.as.classpath.core.jee.J2EE13ClasspathContainerInitializer.J2EE13ClasspathContainer;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class J2EE50ClasspathContainerInitializer extends
		AbstractClasspathContainerInitializer {

	public String getDescription(IPath containerPath, IJavaProject project) {
		return "J2EE 5.0 Classpath Container Initializer";//$NON-NLS-1$
	}

	protected AbstractClasspathContainer createClasspathContainer(IPath path) {
		return new J2EE50ClasspathContainer(path, javaProject);
	}

	protected String getClasspathContainerID() {
		return J2EE50ClasspathContainer.CLASSPATH_CONTAINER;
	}

	public static class J2EE50ClasspathContainer extends AbstractClasspathContainer {
		public final static String SUFFIX = "javaee-5.0";//$NON-NLS-1$
		public final static String CLASSPATH_CONTAINER = CLASSPATH_CONTAINER_PREFIX
				+ "." + J2EE50ClasspathContainer.SUFFIX;//$NON-NLS-1$
		public final static String DESCRIPTION = Messages.J2EE50ClasspathContainerInitializer_description;

		public J2EE50ClasspathContainer(IPath path, IJavaProject javaProject) {
			super(path, DESCRIPTION, SUFFIX, javaProject);
		}
		
		@Override
		public void refresh() {
			new J2EE50ClasspathContainer(path,javaProject).install();
		}
	}
}
