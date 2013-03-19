/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.test.views;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;

import junit.framework.TestCase;

public class ProjectsArchiveViewTest extends TestCase {
	
	public void testArchivesViewIsOpened() throws PartInitException {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IViewPart view = page.showView(ProjectArchivesCommonView.ID);
		assertTrue(view instanceof ProjectArchivesCommonView);
	}
}
