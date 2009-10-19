package org.jboss.ide.eclipse.as.ssh.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentEditorTab;
import org.jboss.ide.eclipse.as.ui.editor.ModuleDeploymentPage;

public class DeploymentPage extends ModuleDeploymentPage {
	protected IDeploymentEditorTab[] createTabs(FormToolkit toolkit, TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		IDeploymentEditorTab tab = new SSHDeploymentModuleTab();
	    tabItem.setText(tab.getTabName());
		tab.setDeploymentPage(this);
		tab.setDeploymentPrefs(preferences);
	    tabItem.setControl(tab.createControl(tabFolder));
	    toolkit.adapt((Composite)tabItem.getControl());

	    return new IDeploymentEditorTab[] { tab };
	}
}
