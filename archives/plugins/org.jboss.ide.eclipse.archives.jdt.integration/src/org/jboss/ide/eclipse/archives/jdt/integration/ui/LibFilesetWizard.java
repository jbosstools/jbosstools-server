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
package org.jboss.ide.eclipse.archives.jdt.integration.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.jdt.integration.model.IArchiveLibFileSet;
import org.jboss.ide.eclipse.archives.jdt.integration.model.LibFileSetNodeProvider;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PackagesUIPlugin;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class LibFilesetWizard extends Wizard {

	private LibFilesetInfoWizardPage page1;
	private IArchiveLibFileSet fileset;
	private IArchiveNode parentNode;

	public LibFilesetWizard(IArchiveLibFileSet fileset, IArchiveNode parentNode) {
		this.fileset = fileset;
		this.parentNode = parentNode;
		setWindowTitle(ArchivesUIMessages.LibFilesetWizard);
	}

	public boolean performFinish() {
		final boolean createFileset = this.fileset == null;

		if (createFileset)
			this.fileset = LibFileSetNodeProvider.createLibFileset();
		fillFilesetFromPage(fileset);
		try {
			getContainer().run(true, false, new IRunnableWithProgress () {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (createFileset)
						parentNode.addChild(fileset);
					try {
						ArchivesModel.instance().getRoot(fileset.getProjectPath()).save(monitor);
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

	private void fillFilesetFromPage (IArchiveLibFileSet fileset) {
		fileset.setId(page1.getId());
	}

	public void addPages() {
		page1 = new LibFilesetInfoWizardPage(getShell(), fileset, parentNode);
		addPage(page1);
	}
}
