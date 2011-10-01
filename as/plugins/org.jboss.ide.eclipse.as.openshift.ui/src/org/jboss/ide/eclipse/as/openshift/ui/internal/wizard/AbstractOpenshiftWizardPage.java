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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftImages;
import org.jboss.tools.common.ui.databinding.ParametrizableWizardPageSupport;

/**
 * @author André Dietisheim
 */
public abstract class AbstractOpenshiftWizardPage extends WizardPage {

	protected ServerAdapterWizardModel model;

	protected AbstractOpenshiftWizardPage(String title, String description, String pageName, IWizard wizard, ServerAdapterWizardModel model) {
		super(pageName);
		this.model = model;
		setWizard(wizard);
		setTitle(title);
		setDescription(description);
		setImageDescriptor(OpenshiftImages.OPENSHIFT_LOGO_WHITE);
	}

	@Override
	public void createControl(Composite parent) {
		final DataBindingContext dbc = new DataBindingContext();
		ParametrizableWizardPageSupport.create(
				IStatus.ERROR | IStatus.INFO | IStatus.WARNING | IStatus.CANCEL, this,
				dbc);
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		container.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				onPageVisible(dbc);
			}
		});
		doCreateControls(container, dbc);
	}
	
	protected void onPageVisible(DataBindingContext dbc) {
	}

	protected abstract void doCreateControls(Composite parent, DataBindingContext dbc);
}
