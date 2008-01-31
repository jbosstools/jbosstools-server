package org.jboss.ide.eclipse.as.test.model;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class JEEClasspathContainerTest extends TestCase {
	
	private static final int ORIGINAL_ENTRIES = 3;

	private TestProjectProvider provider;
	private IProject project;

	protected void setUp() throws Exception {
		provider = new TestProjectProvider("org.jboss.ide.eclipse.as.test", null, "basicwebproject", true); 
		project = provider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}

	
	public void testJEE13ClasspathContainer() {
		testGenericClasspathContainer("org.jboss.ide.eclipse.as.classpath.core.j2ee-1.3", 7);
	}
	
	public void testJEE14ClasspathContainer() {
		testGenericClasspathContainer("org.jboss.ide.eclipse.as.classpath.core.j2ee-1.4", 8);
	}

	public void testJEE50ClasspathContainer() {
		testGenericClasspathContainer("org.jboss.ide.eclipse.as.classpath.core.javaee-5.0", 1);
	}

	
	protected void testGenericClasspathContainer(String containerPath, int expectedEntries) {
		try {
			IJavaProject jproject = JavaCore.create(project);
			IPath path = new Path(containerPath);
			verifyContainerEntries(path, jproject, expectedEntries);
			verifyRawClasspathCount(jproject, ORIGINAL_ENTRIES);
			verifyNotIncludedEntry(jproject, path);
			int beforeRawCount = jproject.getRawClasspath().length;
			int beforeResolvedCount = jproject.getResolvedClasspath(true).length;
			addContainer(jproject, path);
			assertEquals(beforeRawCount+1, jproject.getRawClasspath().length);
			assertEquals(beforeResolvedCount+expectedEntries, jproject.getResolvedClasspath(true).length);
			beforeRawCount = jproject.getRawClasspath().length;
			beforeResolvedCount = jproject.getResolvedClasspath(true).length;
			removeContainer(jproject, path);
			assertEquals(beforeRawCount-1, jproject.getRawClasspath().length);
			assertEquals(beforeResolvedCount-expectedEntries, jproject.getResolvedClasspath(true).length);
			
		} catch( JavaModelException jme ) {
			fail("Exception: " + jme.getMessage());
		} catch( CoreException ce ) {
			fail("Exception: " + ce.getMessage());
		}

	}

	protected void verifyContainerEntries(IPath path, IJavaProject jproject, int expected) throws JavaModelException {
		IClasspathContainer cpc = JavaCore.getClasspathContainer(path, jproject);
		IClasspathEntry[] entries = cpc.getClasspathEntries();
		assertEquals("Received unexpected number of entries", expected, entries.length );
	}
	
	protected void verifyRawClasspathCount(IJavaProject jproject, int count) throws JavaModelException {
		IClasspathEntry[] projectEntry = jproject.getRawClasspath();
		assertEquals("Project should start with only 3 classpath entries", count, projectEntry.length);
	}
	
	protected void verifyNotIncludedEntry(IJavaProject jproject, IPath path) throws JavaModelException {
		IClasspathEntry[] projectEntry = jproject.getRawClasspath();
		for( int i = 0; i < projectEntry.length; i++ ) {
			if( projectEntry[i].getPath().toOSString().startsWith(path.toOSString())) {
				assertFalse("Project prematurely includes classpath", true);
			}
		}
	}
	
	protected void addContainer(IJavaProject jproject, IPath path) throws JavaModelException {
		ArrayList tmp = new ArrayList();
		tmp.addAll(Arrays.asList(jproject.getRawClasspath()));
		tmp.add(JavaCore.newContainerEntry(path));
		jproject.setRawClasspath((IClasspathEntry[]) tmp.toArray(new IClasspathEntry[tmp.size()]), null);
	}
	
	protected void removeContainer(IJavaProject jproject, IPath path) throws JavaModelException {
		ArrayList tmp = new ArrayList();
		tmp.addAll(Arrays.asList(jproject.getRawClasspath()));
		tmp.remove(JavaCore.newContainerEntry(path));
		jproject.setRawClasspath((IClasspathEntry[]) tmp.toArray(new IClasspathEntry[tmp.size()]), null);
	}
}
