package org.jboss.ide.eclipse.archives.test.projects;

import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class JBIDE2439Test extends TestCase {
	private TestProjectProvider aProjectProvider, bProjectProvider;
	private IProject aProject, bProject;

	protected void setUp() throws Exception {
		aProjectProvider = new TestProjectProvider(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2439a",
				null, true);
		aProject = aProjectProvider.getProject();
		aProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());


		bProjectProvider = new TestProjectProvider(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2439b",
				null, true);
		bProject = bProjectProvider.getProject();
		bProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		IResource folder = bProject.findMember("linked");
		if( folder != null ) {
			String linkedLocation = folder.getLocation().toString();
			IFolder folder2 = aProject.getFolder("test");
			folder2.createLink(folder.getLocation(), IResource.FORCE, null);
			aProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
	}


	protected void tearDown() throws Exception {
		aProjectProvider.dispose();
		bProjectProvider.dispose();
	}

	public void testJBIDE2439() {
		ArchiveBuildDelegate delegate = new ArchiveBuildDelegate();
		try {
			delegate.fullProjectBuild(aProject.getLocation(), new NullProgressMonitor());
			aProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			IResource outs = aProject.getFolder("outputs");
			final ArrayList<IResource> list = new ArrayList<IResource>();
			outs.accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if( resource instanceof IFile ) {
						if( !resource.getFullPath().toString().contains(".svn"))
							list.add(resource);
					}
					return true;
				}
			});
			assertEquals(5, list.size());
		} catch( AssertionFailedError afe ) {
			throw afe;
		} catch( RuntimeException re ) {
			fail(re.getMessage());
		} catch( CoreException ce ) {
			fail(ce.getMessage());
		}

	}
}
