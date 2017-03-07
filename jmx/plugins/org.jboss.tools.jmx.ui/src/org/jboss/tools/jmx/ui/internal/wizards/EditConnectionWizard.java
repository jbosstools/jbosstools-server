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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizard;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.ConnectionWizardPage;
import org.jboss.tools.jmx.ui.IEditableConnectionWizardPage;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.UIExtensionManager;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionProviderUI;

public class EditConnectionWizard extends TaskWizard {
	
	public EditConnectionWizard(IConnectionWrapper connection) {
		super(Messages.EditConnectionWizardTitle, new EditConnectionFragment(connection));
	}
	
	private static class EditConnectionFragment extends WizardFragment {
		private IConnectionWrapper con;
		private IEditableConnectionWizardPage rootPage;
		
		public EditConnectionFragment(IConnectionWrapper connection) {
			this.con = connection;
		}
		protected void createChildFragments(List<WizardFragment> list) {
			ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(con.getProvider().getId());
			WizardFragment root = ui.createFragments();
			if( root instanceof IEditableConnectionWizardPage) {
				((IEditableConnectionWizardPage)root).setInitialConnection(con);
				list.add(root);
				rootPage = (IEditableConnectionWizardPage)root;
			}
		}
		

		@Override
		public void performFinish(IProgressMonitor mon) {
			try {
				IConnectionWrapper xinda = ((ConnectionWizardPage)rootPage).getConnection();
				if( xinda == con)
					con.getProvider().connectionChanged(con);
				else {
					con.getProvider().removeConnection(con);
					con.getProvider().addConnection(xinda);
				}
			} catch( CoreException ce) {
				JMXUIActivator.getDefault().getLog().log(
						new Status(IStatus.ERROR, JMXUIActivator.PLUGIN_ID, 
								"Error editing JMX connection", ce)); //$NON-NLS-1$
			}
		}
	}


}
