package org.jboss.ide.eclipse.as.classpath.core.jee;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

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
