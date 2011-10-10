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
package org.jboss.tools.openshift.express.internal.ui.wizard;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.common.ui.databinding.ParametrizableWizardPageSupport;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftImages;

/**
 * @author André Dietisheim
 */
public abstract class AbstractOpenshiftWizardPage extends WizardPage {

	private DataBindingContext dbc;

	protected AbstractOpenshiftWizardPage(String title, String description, String pageName, IWizard wizard) {
		super(pageName);
		setWizard(wizard);
		setTitle(title);
		setDescription(description);
		setImageDescriptor(OpenshiftImages.OPENSHIFT_LOGO_WHITE_MEDIUM);
	}

	@Override
	public void createControl(Composite parent) {
		this.dbc = new DataBindingContext();
		ParametrizableWizardPageSupport.create(
				IStatus.ERROR | IStatus.INFO | IStatus.WARNING | IStatus.CANCEL, this,
				dbc);
		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(6,6).applyTo(container);
		Composite child = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(child);
		setControl(container);
		initPageChangedListener();
		doCreateControls(child, dbc);
	}

	protected void initPageChangedListener() {
		IWizardContainer wizardContainer = getContainer();
		if (wizardContainer instanceof WizardDialog) {
			((WizardDialog) getContainer()).addPageChangedListener(new IPageChangedListener() {

				@Override
				public void pageChanged(PageChangedEvent event) {
					if (event.getSelectedPage() == AbstractOpenshiftWizardPage.this) {
						onPageActivated(dbc);
					} else {
						onPageDeactivated(dbc);
					}
				}
			});
		}
	}

	protected DataBindingContext getDatabindingContext() {
		return dbc;
	}

	protected void onPageActivated(DataBindingContext dbc) {
	}
	
	protected void onPageDeactivated(DataBindingContext dbc) {
	}


	protected abstract void doCreateControls(Composite parent, DataBindingContext dbc);
}
