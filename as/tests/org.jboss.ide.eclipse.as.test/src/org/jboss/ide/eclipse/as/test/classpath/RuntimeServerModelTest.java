/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.test.classpath;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;

public class RuntimeServerModelTest extends TestCase {

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201340
	// skipped since reported issue and always failing
	public void skip_testDoubleCreateEclipseBug201340() throws CoreException {
		createGenericRuntime(ASTest.TOMCAT_RUNTIME_55);
		createGenericRuntime(IJBossToolingConstants.AS_42);
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
	
	public void testCreateBrokenRuntime() throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null,null, IJBossToolingConstants.AS_42);
		assertEquals("expects only one runtime type for jboss 4.2", runtimeTypes.length, 1);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy jbossRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime savedRuntime = jbossRuntime.save(true, new NullProgressMonitor());
		assertEquals("Neither vm install nor configuration is set - should not be able to validate",savedRuntime.validate(null).getSeverity(), Status.ERROR);				
	}
	

}
