package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;

public interface IDeploymentEditorTab {
	public void setDeploymentPage(ModuleDeploymentPage page);
	public void setDeploymentPrefs(DeploymentPreferences prefs);
	public Control createControl(Composite parent);
	public String getTabName();
}
