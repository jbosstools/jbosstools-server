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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.BrowserUtil;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.common.ui.databinding.DataBindingUtils;

/**
 * @author André Dietisheim
 */
public class NewDomainWizardPage extends AbstractOpenshiftWizardPage implements ISkipableWizardPage {

	private static final String OPENSHIFT_EXPRESS_SIGNUP_URL = "https://openshift.redhat.com/app/user/new/express"; //$NON-NLS-1$

	private static final String DIRECTORY_SSH_KEYS = ".ssh";
	private static final String FILTEREXPRESSION_PUBLIC_SSH_KEY = "*.pub";
	private static final String FILTERNAME_PUBLIC_SSH_KEY = "Public ssh key file (*.pub)";

	private NewDomainWizardPageModel model;

	public NewDomainWizardPage(IWizard wizard, ServerAdapterWizardModel wizardModel) {
		super("New Domain", "Please create a new domain",
				"new Domain", wizard);
		this.model = new NewDomainWizardPageModel(wizardModel);
	}

	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(3).margins(10, 10).applyTo(container);

		Label namespaceLabel = new Label(container, SWT.NONE);
		namespaceLabel.setText("&Domain name");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(namespaceLabel);
		Text namespaceText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(namespaceText);
		DataBindingUtils.bindMandatoryTextField(namespaceText, "Domain name",
				NewDomainWizardPageModel.PROPERTY_NAMESPACE, model, dbc);

		Label sshKeyLabel = new Label(container, SWT.NONE);
		sshKeyLabel.setText("SSH Key");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(sshKeyLabel);
		Text sshKeyText = new Text(container, SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(sshKeyText);
		DataBindingUtils.bindMandatoryTextField(sshKeyText, "SSH Key", NewDomainWizardPageModel.PROPERTY_SSHKEY, model,
				dbc);
		Button browseSShKeyButton = new Button(container, SWT.PUSH);
		browseSShKeyButton.setText("Browse");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(100, SWT.DEFAULT).applyTo(browseSShKeyButton);
		browseSShKeyButton.addSelectionListener(onBrowseSshKey());

		Label spacerLabel = new Label(container, SWT.None);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(spacerLabel);

		Button createButton = new Button(container, SWT.NONE);
		createButton.setText("&Create New Domain");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).span(2, 1).indent(0, 10).hint(160, 34)
				.applyTo(createButton);
		createButton.addSelectionListener(onCreate(dbc));
		DataBindingUtils.bindButtonEnablementToValidationStatus(createButton, dbc);
	}

	private SelectionListener onBrowseSshKey() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell);
				dialog.setFilterPath(getSshKeysDirectory());
				dialog.setFilterNames(new String[] { FILTERNAME_PUBLIC_SSH_KEY });
				dialog.setFilterExtensions(new String[] { FILTEREXPRESSION_PUBLIC_SSH_KEY });
				String sshKeyPath = dialog.open();
				if (sshKeyPath != null) {
					model.setSshKey(sshKeyPath);
				}
				;
			}
		};
	}

	private String getSshKeysDirectory() {
		String userHome = System.getProperty("user.home");
		File sshKeysDirectory = new File(userHome, DIRECTORY_SSH_KEYS);
		return sshKeysDirectory.getAbsolutePath();
	}

	protected SelectionAdapter onCreate(final DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					WizardUtils.runInWizard(
							new Job("Creating new domain") {

								@Override
								protected IStatus run(IProgressMonitor monitor) {
									try {
										model.createDomain();
									} catch (OpenshiftException e) {
										return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, NLS.bind(
												"Could not create a new domain with the name \"{0}\"",
												model.getNamespace()), e);
									}
									return Status.OK_STATUS;
								}
							}, getWizard().getContainer(), dbc);
				} catch (Exception ex) {
					// ignore
				}
			};
		};
	}

	protected SelectionAdapter onSignupLinkClicked() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				BrowserUtil.checkedCreateInternalBrowser(OPENSHIFT_EXPRESS_SIGNUP_URL, OPENSHIFT_EXPRESS_SIGNUP_URL,
						OpenshiftUIActivator.PLUGIN_ID, OpenshiftUIActivator.getDefault().getLog());
				getWizard().getContainer().getShell().close();
			}
		};
	}

	protected IObservableCollection toObservableCollection(ValidationStatusProvider... validationStatusProviders) {
		WritableList validationProviders = new WritableList();
		for (ValidationStatusProvider provider : validationStatusProviders) {
			validationProviders.add(provider);
		}
		return validationProviders;
	}

	@Override
	public boolean isSkip() {
		if (!model.hasUser()) {
			return false;
		}
		
		final BlockingQueue<Boolean> queue = new ArrayBlockingQueue<Boolean>(1);
		try {
			WizardUtils.runInWizard(
					new Job("Checking presence of domain") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								queue.offer(model.hasDomain());
							} catch (Exception e) {
								queue.offer(false);
								return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
										"Could not get domain", e);
							} 
							return Status.OK_STATUS;
						}
					}, getWizard().getContainer(), getDatabindingContext());
		} catch (Exception ex) {
			// ignore
		}
		return queue.poll();
	}
}
