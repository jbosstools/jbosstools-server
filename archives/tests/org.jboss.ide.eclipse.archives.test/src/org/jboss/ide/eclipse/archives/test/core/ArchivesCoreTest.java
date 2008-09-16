/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.archives.test.core;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.WorkspaceArchivesCore;
import org.jboss.ide.eclipse.archives.core.ant.AntArchivesCore;

/**
 * This class is just going to test that upon startup
 * of the test suite, a WorkspaceArchivesCore is in place.
 * 
 * It will also test to make sure that if we create a new 
 * ArchivesCore, it can be set as the default so that we can
 * properly test the ant implementation. 
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class ArchivesCoreTest extends TestCase {
	private static WorkspaceArchivesCore workspaceCore;
	private static AntArchivesCore antCore;
	
	public void testArchivesCore() {
		ArchivesCore core = ArchivesCore.getInstance();
		assertEquals(ArchivesCore.WORKSPACE, core.getRunType());
		assertEquals(WorkspaceArchivesCore.class, core.getClass());
		workspaceCore = (WorkspaceArchivesCore)core;
		
		AntArchivesCore core2 = new AntArchivesCore();
		ArchivesCore.setInstance(core2);
		assertEquals(ArchivesCore.STANDALONE, ArchivesCore.getInstance().getRunType());
		antCore = core2;
		
		ArchivesCore.setInstance(core);
	}

	/**
	 * @return the workspaceCore
	 */
	public static WorkspaceArchivesCore getWorkspaceCore() {
		return workspaceCore;
	}

	/**
	 * @return the antCore
	 */
	public static AntArchivesCore getAntCore() {
		return antCore;
	}
}
