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
package org.jboss.tools.jmx.ui.internal.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.ConnectionWizardPage;
import org.jboss.tools.jmx.ui.IEditableConnectionWizardPage;
import org.jboss.tools.jmx.ui.UIExtensionManager;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionProviderUI;

public class EditConnectionWizard extends Wizard {
	private IConnectionWrapper connection;
	public EditConnectionWizard(IConnectionWrapper connection) {
		super();
		this.connection = connection;
	}
	public String getWindowTitle() {
		return "TEST TITLE"; //$NON-NLS-1$
	}

	public void addPages() {
		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(connection.getProvider().getId());
		ConnectionWizardPage[] pages = ui.createPages();
		for( int j = 0; j < pages.length; j++ )
			if( pages[j] instanceof IEditableConnectionWizardPage) {
				((IEditableConnectionWizardPage)pages[j]).setInitialConnection(connection);
				addPage(pages[j]);
			}
	}

	@Override
	public boolean performFinish() {
		try {
			IConnectionWrapper xinda = ((ConnectionWizardPage)getPages()[0]).getConnection();
			if( xinda == connection)
				connection.getProvider().connectionChanged(connection);
			else {
				connection.getProvider().removeConnection(connection);
				connection.getProvider().addConnection(xinda);
			}
			return true;
		} catch( CoreException ce) {
		}
		return false;
	}

}
