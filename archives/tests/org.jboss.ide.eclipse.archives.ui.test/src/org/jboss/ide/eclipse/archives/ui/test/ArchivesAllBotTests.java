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
package org.jboss.ide.eclipse.archives.ui.test;

import org.jboss.ide.eclipse.archives.ui.test.bot.ArchivePreferencesTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.ArchiveViewReSwitchingTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.ArchivesSupportTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.BuildingArchiveNode;
import org.jboss.ide.eclipse.archives.ui.test.bot.BuildingArchiveTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.BuildingProjectTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.CreatingArchiveTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.DeletingArchiveTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.FilesetTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.FolderTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.ModifyingArchiveTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.UserLibrariesFilesetTest;
import org.jboss.ide.eclipse.archives.ui.test.bot.ViewIsPresentTest;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Jaroslav Jankovic
 */
@RunWith(RedDeerSuite.class)
@SuiteClasses({
	ViewIsPresentTest.class,
	ArchivePreferencesTest.class,
	ArchiveViewReSwitchingTest.class,
	FolderTest.class,
	FilesetTest.class,
	UserLibrariesFilesetTest.class,
	ArchivesSupportTest.class,
	BuildingArchiveNode.class,
	BuildingProjectTest.class,
	BuildingArchiveTest.class,
	CreatingArchiveTest.class,
	DeletingArchiveTest.class, 
	ModifyingArchiveTest.class,
	//VariousProjectsArchiving.class,
	//DeployingArchiveTest.class,
})
public class ArchivesAllBotTests {
		
}
