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

import java.io.File;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
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
import org.jboss.tools.common.ui.databinding.DataBindingUtils;

/**
 * @author André Dietisheim
 */
public class NewDomainWizardPage extends AbstractOpenshiftWizardPage {

	private static final String DIRECTORY_SSH_KEYS = ".ssh";
	private static final String FILTEREXPRESSION_PUBLIC_SSH_KEY = "*.pub";
	private static final String FILTERNAME_PUBLIC_SSH_KEY = "Public ssh key file (*.pub)";

	private NewDomainWizardPageModel model;

	public NewDomainWizardPage(String namespace, ServerAdapterWizardModel wizardModel, IWizard wizard) {
		super("Domain", "Create a new domain", "New Domain", wizard);
		this.model = new NewDomainWizardPageModel(namespace, wizardModel);
	}

	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);

		Label namespaceLabel = new Label(container, SWT.NONE);
		namespaceLabel.setText("&Domain name");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(namespaceLabel);
		Text namespaceText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(namespaceText);
		DataBindingUtils.bindMandatoryTextField(
				namespaceText, "Domain name", NewDomainWizardPageModel.PROPERTY_NAMESPACE, model, dbc);

		Label sshKeyLabel = new Label(container, SWT.NONE);
		sshKeyLabel.setText("SSH Key");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(sshKeyLabel);
		Text sshKeyText = new Text(container, SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(sshKeyText);

		Binding sshKeyBinding = dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(sshKeyText)
				, BeanProperties.value(NewDomainWizardPageModel.PROPERTY_SSHKEY).observe(model)
				, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER)
				, null
				);
		ControlDecorationSupport.create(sshKeyBinding, SWT.TOP | SWT.LEFT);

		Button browseSShKeyButton = new Button(container, SWT.PUSH);
		browseSShKeyButton.setText("Browse");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(100, SWT.DEFAULT).applyTo(browseSShKeyButton);
		browseSShKeyButton.addSelectionListener(onBrowseSshKey());
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
			}
		};
	}

	private String getSshKeysDirectory() {
		String userHome = System.getProperty("user.home");
		File sshKeysDirectory = new File(userHome, DIRECTORY_SSH_KEYS);
		return sshKeysDirectory.getAbsolutePath();
	}
}
