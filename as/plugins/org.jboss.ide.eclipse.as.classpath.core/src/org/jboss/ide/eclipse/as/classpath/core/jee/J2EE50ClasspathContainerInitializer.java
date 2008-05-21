/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.classpath.core.jee;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

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
		return new J2EE50ClasspathContainer(path);
	}

	protected String getClasspathContainerID() {
		return J2EE50ClasspathContainer.CLASSPATH_CONTAINER;
	}

	public static class J2EE50ClasspathContainer extends AbstractClasspathContainer {
		public final static String SUFFIX = "javaee-5.0";//$NON-NLS-1$
		public final static String CLASSPATH_CONTAINER = CLASSPATH_CONTAINER_PREFIX
				+ "." + J2EE50ClasspathContainer.SUFFIX;//$NON-NLS-1$
		public final static String DESCRIPTION = "J2EE 5.0 Libraries (JBoss-IDE)";

		public J2EE50ClasspathContainer(IPath path) {
			super(path, DESCRIPTION, SUFFIX);
		}
	}
}
