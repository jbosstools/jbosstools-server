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

import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.WizardUtils;

/**
 * @author André Dietisheim
 */
public class NewApplicationDialog extends Wizard {

	private NewApplicationWizardPageModel newApplicationModel;

	public NewApplicationDialog(IUser user) {
		this.newApplicationModel = new NewApplicationWizardPageModel(user);
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		final ArrayBlockingQueue<Boolean> queue = new ArrayBlockingQueue<Boolean>(1);
		try {
			WizardUtils.runInWizard(new Job("Creating application") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						newApplicationModel.createApplication();
						queue.offer(true);
					} catch (OpenshiftException e) {
						queue.offer(false);
						return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
								NLS.bind("Could not create application \"{0}\"", newApplicationModel.getName()), e);
					}
					return Status.OK_STATUS;
				}
			}, getContainer());
		} catch (Exception e) {
			// ignore
		}
		return queue.poll();
	}

	@Override
	public void addPages() {
		addPage(new NewApplicationWizardPage(newApplicationModel, this));
	}
}
