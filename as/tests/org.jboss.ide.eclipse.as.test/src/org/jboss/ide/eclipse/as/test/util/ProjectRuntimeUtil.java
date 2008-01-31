package org.jboss.ide.eclipse.as.test.util;

import java.util.Collections;
import java.util.HashSet;

import junit.framework.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

public class ProjectRuntimeUtil extends Assert {
	
	public static void clearRuntime(IProject theProject) throws CoreException {
		IFacetedProject facetedProject = ProjectFacetsManager.create(theProject);
		facetedProject.setTargetedRuntimes(new HashSet<org.eclipse.wst.common.project.facet.core.runtime.IRuntime>(), null); 
		facetedProject.setTargetedRuntimes(Collections.EMPTY_SET, new NullProgressMonitor());
	}
	
	public static void setTargetRuntime(IRuntime runtime, IProject theProject) throws CoreException {
		final org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = RuntimeManager.getRuntime(runtime.getId());
		assertNotNull("bridged facet runtime not found", facetRuntime); 
		IFacetedProject facetedProject = ProjectFacetsManager.create(theProject);
		facetedProject.setTargetedRuntimes(new HashSet<org.eclipse.wst.common.project.facet.core.runtime.IRuntime>() { { this.add(facetRuntime);}}, null); 
		facetedProject.setPrimaryRuntime(facetRuntime, null);		
	}

	public static IRuntime createRuntime(String runtimeName, String runtimeTypeId, String asHome) throws CoreException {
		return createRuntime(runtimeName, runtimeTypeId, asHome, "default");
	}

	public static IRuntime createRuntime(String runtimeName, String runtimeTypeId, String asHome, String configuration) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, runtimeTypeId);
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		IRuntimeType runtimeType = runtimeTypes[0];
		RuntimeWorkingCopy jbossRuntime = (RuntimeWorkingCopy)runtimeType.createRuntime(runtimeName, new NullProgressMonitor());
		jbossRuntime.setLocation(new Path(asHome));
		jbossRuntime.setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, configuration);
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		assertEquals(savedRuntime.validate(null).getCode(), Status.OK);
		return savedRuntime;		
	}

}
