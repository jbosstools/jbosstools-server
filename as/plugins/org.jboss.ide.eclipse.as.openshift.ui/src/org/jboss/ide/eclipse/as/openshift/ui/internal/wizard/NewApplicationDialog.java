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

import org.eclipse.jface.wizard.Wizard;
import org.jboss.ide.eclipse.as.openshift.core.IUser;

/**
 * @author André Dietisheim
 */
public class NewApplicationDialog extends Wizard {

	private IUser user;

	public NewApplicationDialog(IUser user) {
		this.user = user;
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		addPage(new NewApplicationWizardPage(new NewApplicationWizardPageModel(user), this));
	}
}
