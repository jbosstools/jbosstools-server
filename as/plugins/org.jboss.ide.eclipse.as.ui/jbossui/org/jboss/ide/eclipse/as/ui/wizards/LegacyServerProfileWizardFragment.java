/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.wizards;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;

/**
 * This is an extension of the {@link ServerProfileWizardFragment}
 * which is capable of showing details about the runtime that's selected.
 * 
 * This one does not re-organize the profile combo the way the as7/wf version does. 
 */

public class LegacyServerProfileWizardFragment extends ServerProfileWizardFragment {

	public LegacyServerProfileWizardFragment() {
		super();
	}

	private RuntimeDetailsComposite rtDetails;
	
	@Override
	protected void addRuntimeDetailsGroup(Composite parent) {
		Group g = new Group(parent, SWT.None);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = 500;
		g.setLayoutData(gd);
		g.setLayout(new GridLayout(1, true));
		rtDetails = new RuntimeDetailsComposite(g, SWT.NONE);
		g.setText("Runtime Details");
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		rtDetails.update(rt);
	}

	private class RuntimeDetailsComposite extends Composite {
		private Label jreVal, homedirVal, basedirVal, configVal;
		RuntimeDetailsComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new GridLayout(2, false));
			
			Label jre= new Label(this, SWT.NONE);
			jre.setText("JRE: ");
			jreVal = new Label(this, SWT.NONE);
			
			Label homedir = new Label(this, SWT.NONE);
			homedir.setText("Home Directory: ");
			homedirVal = new Label(this, SWT.NONE);
			
			Label basedir = new Label(this, SWT.NONE);
			basedir.setText("Base Directory: ");
			basedirVal = new Label(this, SWT.NONE);
			
			Label config= new Label(this, SWT.NONE);
			config.setText("Configuration File: ");
			configVal = new Label(this, SWT.NONE);
		}
		
		public void update(IRuntime rt) {
			LocalJBossServerRuntime jbsrt = rt == null ? null : 
				(LocalJBossServerRuntime) rt.loadAdapter(LocalJBossServerRuntime.class, new NullProgressMonitor());
			if( !jreVal.isDisposed()) {
				if( jbsrt != null ) {
					jreVal.setText(jbsrt.getVM().getInstallLocation().getAbsolutePath());
					homedirVal.setText(rt.getLocation().toOSString());
					basedirVal.setText(jbsrt.getConfigLocation());
					configVal.setText(jbsrt.getJBossConfiguration());
				} else {
					jreVal.setText("");
					homedirVal.setText("");
					basedirVal.setText("");
					configVal.setText("");
				}
			}
		}
	}
	
	protected void runtimeComboChanged() {
		super.runtimeComboChanged();
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( rtDetails != null && !rtDetails.isDisposed())
			rtDetails.update(rt);
	}
}
