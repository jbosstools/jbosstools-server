/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.ui.internal.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizard extends AbstractSkippingWizard {

	public ServerAdapterWizard() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Create new Openshift Express Server Adapter");
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		ServerAdapterWizardModel model = new ServerAdapterWizardModel();
		addPage(new CredentialsWizardPage(this, model));
		addPage(new NewDomainWizardPage(this, model));
		addPage(new ApplicationWizardPage(this, model));
	}
}
