package org.jboss.ide.eclipse.as.test;

import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class ASClasspathTest extends TestCase {
	

	private static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home", "/home/max/rhdevstudio/jboss-eap/jboss-as");
	
	private TestProjectProvider provider;
	private IProject project;

	protected void setUp() throws Exception {
		provider = new TestProjectProvider("org.jboss.ide.eclipse.as.test", null, "basicwebproject", true); 
		project = provider.getProject();
		
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	public void testDoubleCreate() throws CoreException {
		
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, "org.jboss.ide.eclipse.as.runtime.42");
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		IRuntimeWorkingCopy jbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		
		IRuntimeWorkingCopy secondJbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime secondSavedRuntime = secondJbossRuntime.save(true, new NullProgressMonitor());
		
		assertEquals(savedRuntime.getName(), secondSavedRuntime.getName());
		assertNotSame(savedRuntime, secondSavedRuntime);				
		assertFalse("Why are two different runtimes created with the same ID ?!", savedRuntime.getId().equals(secondSavedRuntime.getId()));		
		
	}
	public void testCreateBrokenServer() throws CoreException {
	
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, "org.jboss.ide.eclipse.as.runtime.42");
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		IRuntimeWorkingCopy jbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
				
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		
		System.out.println(savedRuntime.getName() + " " + savedRuntime.getId());
		assertEquals("Neither vm install nor configuration is set - should not be able to validate",savedRuntime.validate(null).getCode(), Status.ERROR);				
		
	}
	
	public void testClasspathAvailable() throws CoreException {
		
		IJavaProject javaProject = JavaCore.create(project);
		assertTrue(javaProject.exists());
		
		IServer createServer = createServer();
		setTargetRuntime(createServer, project);
				
		IClasspathEntry paths[] = javaProject.getRawClasspath();
		boolean found = false;
		for (int i = 0; i < paths.length; i++) {
			IClasspathEntry classpathEntry = paths[i];
			if(classpathEntry.getPath().toString().equals("org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer/JBoss 4.2 Runtime")) {
				found = true;
			}
		}
		assertTrue("could not find jboss as specific entry in raw classpath", found);
		
		IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
		
		boolean jsfFound = false;
		for (int i = 0; i < resolvedClasspath.length; i++) {
			IClasspathEntry classpathEntry = resolvedClasspath[i];
			if(classpathEntry.getPath().toString().contains("jsf")) {
				
				jsfFound = true;
			}
			System.out.println(classpathEntry);
		}
		assertTrue("jsf lib not found!", jsfFound);
		
		
	}

	private void setTargetRuntime(IServer createServer, IProject theProject) throws CoreException {
		
		final org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = RuntimeManager.getRuntime(createServer.getRuntime().getId());
		
		assertNotNull("bridged facet runtime not found", facetRuntime); 
		
		IFacetedProject facetedProject = ProjectFacetsManager.create(theProject);
		
		facetedProject.setTargetedRuntimes(new HashSet<org.eclipse.wst.common.project.facet.core.runtime.IRuntime>() { { this.add(facetRuntime);}}, null); 
		facetedProject.setPrimaryRuntime(facetRuntime, null);
		
		
	}

	private IServer createServer() throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, "org.jboss.ide.eclipse.as.runtime.42");
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		RuntimeWorkingCopy jbossRuntime = (RuntimeWorkingCopy)runtimeType.createRuntime(null, new NullProgressMonitor());
		
		jbossRuntime.setLocation(new Path(JBOSS_AS_HOME));
		jbossRuntime.setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, "default");
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		
		System.out.println(savedRuntime.getName() + " " + savedRuntime.getId());
		assertEquals(savedRuntime.validate(null).getCode(), Status.OK);
		
		
		IServerType jboss42serverType = ServerCore.findServerType("org.jboss.ide.eclipse.as.42");
		
		assertNotNull(jboss42serverType);
		
		IServerWorkingCopy jboss42server = jboss42serverType.createServer(null, null, jbossRuntime, new NullProgressMonitor());
		
		assertNotNull(jboss42server);
		
		assertSame(jbossRuntime, jboss42server.getRuntime());
		return jboss42server.save(true,  null);		
	}

}
