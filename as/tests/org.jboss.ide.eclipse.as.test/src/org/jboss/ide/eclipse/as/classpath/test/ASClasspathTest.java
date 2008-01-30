package org.jboss.ide.eclipse.as.classpath.test;

import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.tools.common.test.util.TestProjectProvider;

public class ASClasspathTest extends TestCase {
	

	private static final String ORG_JBOSS_IDE_ECLIPSE_AS_RUNTIME_42 = "org.jboss.ide.eclipse.as.runtime.42";

	private static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home", "/home/max/rhdevstudio/jboss-eap/jboss-as");
	
	private TestProjectProvider provider;
	private IProject project;

	protected void setUp() throws Exception {
		provider = new TestProjectProvider("org.jboss.ide.eclipse.as.test", null, "basicwebproject", true); 
		project = provider.getProject();
		
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201340
	// skipped since reported issue and always failing
	public void skip_testDoubleCreateEclipseBug201340() throws CoreException {
		
		createGenericRuntime("org.eclipse.jst.server.tomcat.runtime.55");
		
		createGenericRuntime(ORG_JBOSS_IDE_ECLIPSE_AS_RUNTIME_42);
		
		
	}

	private IRuntime[] createGenericRuntime(String runtimeId) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, runtimeId);
		assertEquals("expects only one runtime type", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		IRuntimeWorkingCopy firstRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime savedRuntime = firstRuntime.save(true, new NullProgressMonitor());
		
		IRuntimeWorkingCopy secondRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime secondSavedRuntime = secondRuntime.save(true, new NullProgressMonitor());
		
		assertEquals(savedRuntime.getName(), secondSavedRuntime.getName());
		assertNotSame(savedRuntime, secondSavedRuntime);				
		assertFalse("Why are two different runtimes " + runtimeId + " created with the same ID ?!", savedRuntime.getId().equals(secondSavedRuntime.getId()));
		return new IRuntime[] { savedRuntime, secondSavedRuntime };
	}
	public void testCreateBrokenServer() throws CoreException {
	
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, ORG_JBOSS_IDE_ECLIPSE_AS_RUNTIME_42);
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		IRuntimeWorkingCopy jbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
				
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
				
		assertEquals("Neither vm install nor configuration is set - should not be able to validate",savedRuntime.validate(null).getSeverity(), Status.ERROR);				
		
		
	}
	
	// see JBIDE-1355 why this is skipped
	public void skip_testClasspathAvailable() throws CoreException {
		// Weirdness: If this method is the only to run everything works as expected.
		
		IJavaProject javaProject = JavaCore.create(project);
		assertTrue(javaProject.exists());
		
		String id = "cp-runtime";
		IRuntime createdRuntime = createRuntime(id);
		setTargetRuntime(createdRuntime, project);
				
		//assertEquals(createdRuntime.getId(), "cp-runtime");
		
		
		
		IClasspathEntry paths[] = javaProject.getRawClasspath();
		boolean found = false;
		for (int i = 0; i < paths.length; i++) {
			IClasspathEntry classpathEntry = paths[i];
			
			if(classpathEntry.getPath().toString().equals("org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer/" + id)) {
				IClasspathContainer container = JavaCore.getClasspathContainer(classpathEntry.getPath(), javaProject);
				assertEquals("container not returning userclasses!", container.getKind(), container.K_APPLICATION);
				found = true;
			}
		}
		assertTrue("could not find jboss as specific entry in raw classpath", found);
		
		IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
		
		boolean jsfFound = false;
		for (int i = 0; i < resolvedClasspath.length; i++) {
			IClasspathEntry classpathEntry = resolvedClasspath[i];
			if(classpathEntry.getPath().toString().contains("jsf")) {
				System.out.println(classpathEntry);	
				jsfFound = true;
			}
			
		}
		assertTrue("jsf lib not found!", jsfFound);
		
		
	}

	private void setTargetRuntime(IRuntime runtime, IProject theProject) throws CoreException {
		
		final org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = RuntimeManager.getRuntime(runtime.getId());
		
		assertNotNull("bridged facet runtime not found", facetRuntime); 
		
		IFacetedProject facetedProject = ProjectFacetsManager.create(theProject);
		
		facetedProject.setTargetedRuntimes(new HashSet<org.eclipse.wst.common.project.facet.core.runtime.IRuntime>() { { this.add(facetRuntime);}}, null); 
		facetedProject.setPrimaryRuntime(facetRuntime, null);		
		
	}

	private IRuntime createRuntime(String runtimeName) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, ORG_JBOSS_IDE_ECLIPSE_AS_RUNTIME_42);
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		
		IRuntimeType runtimeType = runtimeTypes[0];
		
		RuntimeWorkingCopy jbossRuntime = (RuntimeWorkingCopy)runtimeType.createRuntime(runtimeName, new NullProgressMonitor());
		
		jbossRuntime.setLocation(new Path(JBOSS_AS_HOME));
		jbossRuntime.setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, "default");
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
				
		assertEquals(savedRuntime.validate(null).getCode(), Status.OK);
		
		return savedRuntime;		
	}

}
