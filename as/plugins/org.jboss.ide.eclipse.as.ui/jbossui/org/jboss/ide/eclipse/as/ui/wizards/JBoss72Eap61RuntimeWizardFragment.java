package org.jboss.ide.eclipse.as.ui.wizards;

import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;

public class JBoss72Eap61RuntimeWizardFragment extends JBoss7RuntimeWizardFragment {
	@Override
	protected String getSystemJarPath() {
		return JBossServerType.AS72.getSystemJarPath();
	}

}
