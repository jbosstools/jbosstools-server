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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

public class AdapterWizardPage extends AbstractOpenshiftWizardPage implements IWizardPage {

	private AdapterWizardPageModel model;

	public AdapterWizardPage(ServerAdapterWizard wizard, ServerAdapterWizardModel model) {
		super("Server Adapter", "...", "Server Adapter", wizard);
		this.model = new AdapterWizardPageModel(model);
	}

	@Override
	protected void doCreateControls(Composite parent, DataBindingContext dbc) {
		// TODO Auto-generated method stub
	}

}
