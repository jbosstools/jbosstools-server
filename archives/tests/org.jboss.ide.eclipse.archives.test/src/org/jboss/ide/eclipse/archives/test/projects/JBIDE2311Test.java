package org.jboss.ide.eclipse.archives.test.projects;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class JBIDE2311Test extends TestCase {
	private TestProjectProvider provider;
	private IProject project;

	protected void setUp() throws Exception {
		provider = new TestProjectProvider(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2311",
				null, true);
		project = provider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}

	protected void tearDown() throws Exception {
		provider.dispose();
	}

	public void testJBIDE2311() {
		ArchiveBuildDelegate delegate = new ArchiveBuildDelegate();
		try {
			delegate.fullProjectBuild(project.getLocation(), new NullProgressMonitor());
		} catch( AssertionFailedError afe ) {
			throw afe;
		} catch( RuntimeException re ) {
			fail(re.getMessage());
		}
	}
}
