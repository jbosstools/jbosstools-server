/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards.composite;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.ui.IPreferenceKeys;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;
import org.jboss.tools.as.runtimes.integration.ui.composites.DownloadRuntimeHomeComposite;

public class JBossRuntimeHomeComposite extends DownloadRuntimeHomeComposite {
	public JBossRuntimeHomeComposite(Composite parent, int style, IWizardHandle handle, TaskModel tm) {
		super(parent, style, handle, tm);
	}
	
	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( r == null ) {
			r = (IRuntime) getTaskModel().getObject(ServerProfileWizardFragment.TASK_CUSTOM_RUNTIME);
		}
		return r;
	}

	protected String getDefaultHomeDirectory(IRuntimeType rtt) {
		IEclipsePreferences prefs2 = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String value = prefs2.get(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + rtt.getId(), null);
		return (value != null && value.length() != 0) ? value : "";
	}
}
