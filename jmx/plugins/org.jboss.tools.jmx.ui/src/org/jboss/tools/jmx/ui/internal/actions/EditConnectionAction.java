/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jmx.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.internal.wizards.EditConnectionWizard;

public class EditConnectionAction extends Action {
	private IConnectionWrapper connection;
	public EditConnectionAction(IConnectionWrapper connection) {
		this.connection = connection;
		//setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(key));
		setEnabled(true);
		setText(Messages.EditConnectionAction); 
	}
	public void run() {
		EditConnectionWizard wizard = new EditConnectionWizard(connection);
		WizardDialog d = new WizardDialog(new Shell(), wizard);
		d.open();
	}
}
