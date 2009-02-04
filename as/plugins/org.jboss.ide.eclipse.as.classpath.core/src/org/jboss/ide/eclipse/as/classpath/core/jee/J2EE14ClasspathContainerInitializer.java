package org.jboss.ide.eclipse.as.classpath.core.jee;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.jboss.ide.eclipse.as.classpath.core.jee.J2EE13ClasspathContainerInitializer.J2EE13ClasspathContainer;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class J2EE14ClasspathContainerInitializer extends
		AbstractClasspathContainerInitializer {

	public String getDescription(IPath containerPath, IJavaProject project) {
		return "J2EE 1.4 Classpath Container Initializer";//$NON-NLS-1$
	}

	protected AbstractClasspathContainer createClasspathContainer(IPath path) {
		return new J2EE14ClasspathContainer(path, javaProject);
	}

	protected String getClasspathContainerID() {
		return J2EE14ClasspathContainer.CLASSPATH_CONTAINER;
	}

	public static class J2EE14ClasspathContainer extends AbstractClasspathContainer {
		public final static String SUFFIX = "j2ee-1.4";//$NON-NLS-1$
		public final static String CLASSPATH_CONTAINER = CLASSPATH_CONTAINER_PREFIX
				+ "." + J2EE14ClasspathContainer.SUFFIX;//$NON-NLS-1$
		public final static String DESCRIPTION = "J2EE 1.4 Libraries (JBoss Tools)";

		public J2EE14ClasspathContainer(IPath path, IJavaProject javaProject) {
			super(path, DESCRIPTION, SUFFIX, javaProject);
		}
		
		@Override
		public void refresh() {
			new J2EE14ClasspathContainer(path,javaProject).install();
		}
	}
}
