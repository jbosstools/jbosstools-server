/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.test.bot;

import org.jboss.ide.eclipse.archives.ui.test.bot.util.ExplorerInProjectExplorer;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.junit.BeforeClass;
import org.junit.Test;
 
/**
 * Checks if project archive support can be enabled/disabled on 
 * some project
 * 
 * @author jjankovi
 *
 */
public class ArchivesSupportTest extends ArchivesTestBase {

	private static final String project = "ArchivesSupportTest";
	
	@BeforeClass
	public static void setup() {
		createJavaProject(project);
	}
	
	@Test
	public void testArchiveSupport() {
		testAddArchiveSupport();
		testRemoveArchiveSupport();
	}
	
	private void testAddArchiveSupport() {
		
		addArchivesSupport(project);
		new WaitUntil(new ExplorerInProjectExplorer(project));
	}
	
	private void testRemoveArchiveSupport() {
		
		removeArchivesSupport(project);
		new WaitWhile(new ExplorerInProjectExplorer(project));
		
		
	}
	
}
