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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.WizardUtils;

/**
 * @author André Dietisheim
 */
public class NewApplicationWizardPage extends AbstractOpenshiftWizardPage {

	private static final int NAME_MAXLENGTH = 13;

	private NewApplicationWizardPageModel model;

	public NewApplicationWizardPage(NewApplicationWizardPageModel model, IWizard wizard) {
		super("Create new Openshift Express application", "Create new Openshift Express application",
				"Create new Openshift Express application", wizard);
		this.model = model;
	}

	@Override
	protected void doCreateControls(Composite parent, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(parent);

		Label nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText("Name");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(nameLabel);
		Text nameText = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nameText);

		Label cartridgeLabel = new Label(parent, SWT.WRAP);
		cartridgeLabel.setText("Cartridge");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(cartridgeLabel);
		Combo cartridgesCombo = new Combo(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(cartridgesCombo);
	}

	@Override
	protected void onPageActivated(DataBindingContext dbc) {
		try {
			WizardUtils.runInWizard(new Job("Load cartridges") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						model.loadCartridges();
					} catch (OpenshiftException e) {
						return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, "Could not load cartridges", e);
					}
					return Status.OK_STATUS;
				}
			}, getContainer());
		} catch (Exception e) {
			// ignore
		}
	}

	private final class ApplicationNameValidator implements IInputValidator {

		@Override
		public String isValid(String newText) {
			if (newText.length() > 0
					&& newText.length() <= NAME_MAXLENGTH) {
				return null;
			} else {
				return "you have to provide a valid application name with less than 13 characters";
			}
		}
	};

}