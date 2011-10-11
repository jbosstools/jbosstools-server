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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.common.ui.databinding.DataBindingUtils;
import org.jboss.tools.openshift.express.client.IDomain;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftUIActivator;

/**
 * @author André Dietisheim
 */
public class DomainWizardPage extends AbstractOpenshiftWizardPage {

	private DomainWizardPageModel model;
	private ServerAdapterWizardModel wizardModel;

	public DomainWizardPage(IWizard wizard, ServerAdapterWizardModel wizardModel) {
		super("Domain", "Create a new domain (if you have none yet), use your existing domain or rename it...",
				"new Domain", wizard);
		this.wizardModel = wizardModel;
		this.model = new DomainWizardPageModel(wizardModel);
	}

	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);

		Label namespaceLabel = new Label(container, SWT.NONE);
		namespaceLabel.setText("&Domain name");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(namespaceLabel);
		Text namespaceText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(namespaceText);
		DataBindingUtils.bindMandatoryTextField(
				namespaceText, "Domain name", DomainWizardPageModel.PROPERTY_NAMESPACE, model, dbc);

		// bind the existence of a domain to the page validity
		dbc.bindValue(
				new WritableValue(null, IDomain.class)
				, BeanProperties.value(DomainWizardPageModel.PROPERTY_DOMAIN).observe(model)
				, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER)
				, new UpdateValueStrategy().setAfterGetValidator(new IValidator() {

					@Override
					public IStatus validate(Object value) {
						if (!(value instanceof IDomain)) {
							return ValidationStatus.info("You have no domain yet, you need to create one.");
						}
						return ValidationStatus.ok();
					}
				}));
		
		Label spacerLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(80, 30).applyTo(spacerLabel);

		Button createButton = new Button(container, SWT.PUSH);
		createButton.setText("&Create");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(80, SWT.DEFAULT).applyTo(createButton);
		DataBindingUtils.bindEnablementToValidationStatus(createButton, IStatus.ERROR | IStatus.WARNING | IStatus.INFO , dbc);
		createButton.addSelectionListener(onCreate(dbc));

		Button renameButton = new Button(container, SWT.PUSH);
		renameButton.setText("&Rename");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(80, SWT.DEFAULT).applyTo(renameButton);
		DataBindingUtils.bindEnablementToValidationStatus(renameButton, IStatus.OK, dbc);
		renameButton.addSelectionListener(onRename(dbc));
	}

	private SelectionListener onCreate(DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = getContainer().getShell();
				if (WizardUtils.openWizardDialog(new NewDomainDialog(model.getNamespace(), wizardModel), shell)
						== Dialog.OK) {
				}
			}
		};
	}

	private SelectionListener onRename(DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					WizardUtils.runInWizard(new Job("Renaming domain...") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								model.renameDomain();
								return Status.OK_STATUS;
							} catch (Exception e) {
								return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
										"Could not get domain", e);
							}
						}
					}, getWizard().getContainer(), getDatabindingContext());
				} catch (Exception ex) {
					// ignore
				}
			}
		};
	}

	@Override
	protected void onPageActivated(DataBindingContext dbc) {
		try {
			WizardUtils.runInWizard(new Job("Checking presence of domain...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						model.updateDomain();
						return Status.OK_STATUS;
					} catch (Exception e) {
						return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
								"Could not get domain", e);
					}
				}
			}, getWizard().getContainer(), getDatabindingContext());
		} catch (Exception ex) {
			// ignore
		}
	}
}
