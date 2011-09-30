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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * @author André Dietisheim
 */
public class ApplicationWizardPage extends AbstractOpenshiftWizardPage {

	protected ApplicationWizardPage(IWizard wizard, ServerAdapterWizardModel model) {
		super("Application selection", "Please select an Openshift Express application to use", "Application selection", wizard, model);
	}
	
	@Override
	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(1).margins(10, 10).applyTo(container);
		
	}
}
