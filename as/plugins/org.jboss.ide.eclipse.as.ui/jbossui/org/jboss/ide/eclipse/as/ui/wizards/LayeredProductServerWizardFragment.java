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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss71ServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel.ServerProfile;

/**
 * At the time of this writing, only 4 profiles exist for as7/wf server types:
 * 1) Local
 * 2) Local w management
 * 3) RSE
 * 4) RSE w management
 * 
 * This class attempts to override the superclass with a different UI 
 * to make selecting one of these 4 profiles easier. 
 * This means we're not using the extension points directly, and,
 * if more profiles are added, this UI will be restricting the choices of 
 * the user. 
 * 
 * But for now, it may work. 
 */

public class LayeredProductServerWizardFragment extends ServerProfileWizardFragment {

	public LayeredProductServerWizardFragment() {
		super();
	}

	@Override
	protected IProfileComposite createProfileSection(Composite main) {
		LayeredProfileComposite pc =  new LayeredProfileComposite(main, SWT.NONE, this);
		GridData gd = new GridData();
		gd.widthHint = 500;
		pc.setLayoutData(gd);
		return pc;
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
			LocalJBoss7ServerRuntime jbsrt = rt == null ? null : 
				(LocalJBoss7ServerRuntime) rt.loadAdapter(LocalJBoss7ServerRuntime.class, new NullProgressMonitor());
			if( !jreVal.isDisposed()) {
				if( jbsrt != null ) {
					jreVal.setText(jbsrt.getVM().getInstallLocation().getAbsolutePath());
					homedirVal.setText(rt.getLocation().toOSString());
					basedirVal.setText(jbsrt.getRawBaseDirectory());
					configVal.setText(jbsrt.getConfigurationFile());
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
	
	
	private static class LayeredProfileComposite extends Composite implements IProfileComposite {
		private ServerProfile[] profiles;
		private ServerProfileWizardFragment profileFragment;
		private Button localButton, remoteButton, fsButton, mgmtButton;
		
		LayeredProfileComposite(Composite parent, int style, final ServerProfileWizardFragment profileFragment) {
			super(parent, style);
			setLayout(new GridLayout(2, false));
			this.profileFragment = profileFragment;
			initProfiles();
			
			// First row
			Label localOrRemote = new Label(this, SWT.None);
			GridData lorData = new GridData();
			lorData.verticalAlignment = SWT.BEGINNING;
			localOrRemote.setLayoutData(lorData);
			localOrRemote.setText("The server is: ");
			Composite localOrRemoteComposite = new Composite(this, SWT.NONE);
			localOrRemoteComposite.setLayout(new GridLayout(1,  true));
			localButton = new Button(localOrRemoteComposite, SWT.RADIO);
			remoteButton = new Button(localOrRemoteComposite, SWT.RADIO);
			localButton.setText("Local");
			remoteButton.setText("Remote");
			
			// second row
			Label control = new Label(this, SWT.NONE);
			control.setText("Controlled by: ");
			control.setLayoutData(lorData);
			Composite fsOrMgmtComposite = new Composite(this, SWT.NONE);
			fsOrMgmtComposite.setLayout(new GridLayout(1,  true));
			fsButton = new Button(fsOrMgmtComposite, SWT.RADIO);
			mgmtButton = new Button(fsOrMgmtComposite, SWT.RADIO);
			fsButton.setText("Filesystem and shell operations");
			mgmtButton.setText("Management Operations");
			
			
			
			// Find out the value
			IServerAttributes server = (IServerAttributes)profileFragment.getTaskModel().getObject(TaskModel.TASK_SERVER);
			String currentProfile = ServerProfileModel.getProfile(server, ServerProfileModel.DEFAULT_SERVER_PROFILE);
			setSelectionsForProfile(currentProfile);
			
			SelectionListener sl = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					profileFragment.setProfile(getSelectedProfile());
				}
			};
			localButton.addSelectionListener(sl);
			remoteButton.addSelectionListener(sl);
			fsButton.addSelectionListener(sl);
			mgmtButton.addSelectionListener(sl);
		}
		
		private void setSelectionsForProfile(String currentProfile) {
			if( currentProfile == null ) {
				localButton.setSelection(true);
				fsButton.setSelection(true);
			} else {
				boolean rse = currentProfile.equals("rse") || currentProfile.startsWith("rse.");
				boolean mgmt = currentProfile.endsWith(".mgmt");
				localButton.setSelection(!rse);
				remoteButton.setSelection(rse);
				fsButton.setSelection(!mgmt);
				mgmtButton.setSelection(mgmt);
			}
		}
		
		public ServerProfile getSelectedProfile() {
			boolean local = localButton.getSelection();
			boolean mgmt = mgmtButton.getSelection();
			
			// We'll have to hard-code the profile names here.
			String profile = local ? "local" : "rse";
			if( mgmt ) {
				profile += ".mgmt";
			}
			for( int i = 0; i < profiles.length; i++ ) {
				if( profiles[i].getId().equals(profile))
					return profiles[i];
			}
			return null; // never reached
		}
		
		protected void initProfiles() {
			IServerAttributes server = (IServerAttributes)profileFragment.getTaskModel().getObject(TaskModel.TASK_SERVER);
			String serverType = server.getServerType().getId();
			ServerProfile[] tmpProfiles = ServerProfileModel.getDefault().getProfiles(serverType);
			// sort by visible name
			ArrayList<ServerProfile> tmpProfileList = new ArrayList<ServerProfile>(Arrays.asList(tmpProfiles));
			Collections.sort(tmpProfileList, new Comparator<ServerProfile>(){
				public int compare(ServerProfile arg0, ServerProfile arg1) {
					String n1 = arg0.getVisibleName() == null ? arg0.getId() : arg0.getVisibleName();
					String n2 = arg1.getVisibleName() == null ? arg1.getId() : arg1.getVisibleName();
					return n1.compareTo(n2);
				}
			});
			profiles =  tmpProfileList.toArray(new ServerProfile[tmpProfileList.size()]);
		}
	}

	
	
}
