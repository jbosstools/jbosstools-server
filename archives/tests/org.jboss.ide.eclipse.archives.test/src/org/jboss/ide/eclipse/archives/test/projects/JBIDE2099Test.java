package org.jboss.ide.eclipse.archives.test.projects;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class JBIDE2099Test extends TestCase {
	private TestProjectProvider provider;
	private IProject project;
	private IPath outputWar;
	protected void setUp() throws Exception {
		provider = new TestProjectProvider(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2099",
				null, true);
		project = provider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		outputWar = project.getLocation().append("output").append("dist")
					.append("unified-http-invoker.sar").append("unified-invoker.war");
	}

	protected void tearDown() throws Exception {
		provider.dispose();
	}

	public void testJBIDE2099() {
		ArchiveBuildDelegate delegate = new ArchiveBuildDelegate();
		try {
			delegate.fullProjectBuild(project.getLocation(), new NullProgressMonitor());
			assertTrue(outputWar.toFile().isDirectory());
		} catch( AssertionFailedError afe) {
			throw afe;
		} catch( RuntimeException re ) {
			fail(re.getMessage());
		}
	}
}
