/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
