package org.jboss.ide.eclipse.as.ui.wildflyjar;

import org.apache.maven.model.Plugin;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class WildflyJarPropertyTester extends PropertyTester {

	public WildflyJarPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		System.out.println("Here");
		if("isWildflyJar".equals(property) && receiver instanceof IProject) {
			boolean ret = isWildflyJarProject((IProject)receiver);
			System.out.println(ret);
			return ret;
		}
		return false;
	}

	public static boolean isWildflyJarProject(IProject jp) {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(jp);
		if( facade == null || facade.getMavenProject() == null ) {
			System.out.println("false stuff");
			return false;
		}
		Plugin p = facade.getMavenProject().getPlugin("org.wildfly.plugins:wildfly-jar-maven-plugin");
		if (p != null) {
			return true;
		}
		return false;
	}
}
