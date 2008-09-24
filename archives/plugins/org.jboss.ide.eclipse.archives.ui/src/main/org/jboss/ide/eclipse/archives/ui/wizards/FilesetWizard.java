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
package org.jboss.ide.eclipse.archives.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.jboss.ide.eclipse.archives.core.model.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.archives.ui.wizards.pages.FilesetInfoWizardPage;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class FilesetWizard extends Wizard {

	private FilesetInfoWizardPage page1;
	private IArchiveFileSet fileset;
	private IArchiveNode parentNode;

	public FilesetWizard(IArchiveFileSet fileset, IArchiveNode parentNode)
	{
		this.fileset = fileset;
		this.parentNode = parentNode;
		setWindowTitle(ArchivesUIMessages.FilesetWizard);
	}

	public boolean performFinish() {
		final boolean createFileset = this.fileset == null;

		if (createFileset)
			this.fileset = ArchiveNodeFactory.createFileset();
		fillFilesetFromPage(fileset);
		try {
			getContainer().run(true, false, new IRunnableWithProgress () {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (createFileset)
						parentNode.addChild(fileset);
					try {
						ArchivesModel.instance().save(fileset.getProjectPath(), monitor);
					}  catch( ArchivesModelException ame ) {
						IStatus status = new Status(IStatus.ERROR, PackagesUIPlugin.PLUGIN_ID, ArchivesUIMessages.ErrorCompletingWizard, ame);
						PackagesUIPlugin.getDefault().getLog().log(status);
					}
				}
			});
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch(Exception e) {}
		return true;
	}

	private void fillFilesetFromPage (IArchiveFileSet fileset) {
		fileset.setExcludesPattern(page1.getExcludes());
		fileset.setIncludesPattern(page1.getIncludes());
		fileset.setFlattened(page1.isFlattened());
		fileset.setRawSourcePath(page1.getRawPath());
		fileset.setInWorkspace(page1.isRootDirWorkspaceRelative());
	}

	public void addPages() {
		page1 = new FilesetInfoWizardPage(getShell(), fileset, parentNode);
		addPage(page1);
	}
}
