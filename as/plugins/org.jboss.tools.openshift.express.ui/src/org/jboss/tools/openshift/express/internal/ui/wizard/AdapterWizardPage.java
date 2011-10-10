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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AdapterWizardPage extends AbstractOpenshiftWizardPage implements IWizardPage {

	private AdapterWizardPageModel model;

	public AdapterWizardPage(ServerAdapterWizard wizard, ServerAdapterWizardModel model) {
		super("Server Adapter", "...", "Server Adapter", wizard);
		this.model = new AdapterWizardPageModel(model);
	}

	@Override
	protected void doCreateControls(Composite parent, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().applyTo(parent);

		Group projectGroup = new Group(parent, SWT.BORDER);
		projectGroup.setText("Project setup");
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(projectGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(2).applyTo(projectGroup);

		Label projectNameLabel = new Label(projectGroup, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(projectNameLabel);
		projectNameLabel.setText("Project name");
		Text projectNameText = new Text(projectGroup, SWT.BORDER);
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(projectNameText);

		Label branchNameLabel = new Label(projectGroup, SWT.NONE);
		branchNameLabel.setText("Name of remote branch");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(branchNameLabel);
		Text branchNameText = new Text(projectGroup, SWT.BORDER);
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(branchNameText);

		Group serverAdapterGroup = new Group(parent, SWT.BORDER);
		serverAdapterGroup.setText("JBoss Server adapter");
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(serverAdapterGroup);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 6;
		fillLayout.marginWidth = 6;
		serverAdapterGroup.setLayout(fillLayout);
		Button serverAdapterCheckbox = new Button(serverAdapterGroup, SWT.CHECK);
		serverAdapterCheckbox.setText("Create a JBoss server adapter");
	}
}
