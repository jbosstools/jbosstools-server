package org.jboss.ide.eclipse.as.test.model;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.test.ASTest;

public class RuntimeServerModelTest extends TestCase {

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201340
	// skipped since reported issue and always failing
	public void skip_testDoubleCreateEclipseBug201340() throws CoreException {
		createGenericRuntime(ASTest.TOMCAT_RUNTIME_55);
		createGenericRuntime(ASTest.JBOSS_RUNTIME_42);
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
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, ASTest.JBOSS_RUNTIME_42);
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy jbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		assertEquals("Neither vm install nor configuration is set - should not be able to validate",savedRuntime.validate(null).getSeverity(), Status.ERROR);				
	}
	

}
