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
package org.jboss.ide.eclipse.as.wtp.ui.composites;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanType;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class RuntimeHomeComposite extends Composite {
	
	private IWizardHandle handle;
	private TaskModel tm;
	protected Label homeDirLabel;
	protected Text homeDirText;
	protected Button homeDirButton;
	protected String homeDir;
	private IRuntimeHomeCompositeListener listener;

	public RuntimeHomeComposite(Composite parent, int style, IWizardHandle handle, TaskModel tm) {
		super(parent, style);
		this.handle = handle;
		this.tm = tm;
		createWidgets();
	}
	
	protected TaskModel getTaskModel() {
		return tm;
	}
	
	protected IWizardHandle getWizardHandle() {
		return handle;
	}
	
	public void setListener(IRuntimeHomeCompositeListener listener) {
		this.listener = listener;
	}
	
	public String getHomeDirectory() {
		return homeDir;
	}
	
	protected void homeDirChanged() {
		if( listener != null )
			listener.homeChanged();
	}
	
	protected void createWidgets() {
		// Create our composite
		setLayout(new FormLayout());

		// Create Internal Widgets
		homeDirLabel = new Label(this, SWT.NONE);
		homeDirLabel.setText(Messages.wf_HomeDirLabel);
		homeDirText = new Text(this, SWT.BORDER);
		homeDirButton = new Button(this, SWT.NONE);
		homeDirButton.setText(Messages.browse);

		// Add listeners
		homeDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				homeDir = homeDirText.getText();
				homeDirChanged();
			}
		});

		homeDirButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

			public void widgetSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

		});

		// Set Layout Data
		homeDirLabel.setLayoutData(FormDataUtility.createFormData2(null,0,homeDirText,-5,0,5,null,0));
		homeDirText.setLayoutData(FormDataUtility.createFormData2(homeDirLabel,5,null,0,0,5,homeDirButton,-5));
		homeDirButton.setLayoutData(FormDataUtility.createFormData2(homeDirLabel,5,null,0,null,0,100,0));
	}

	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime) tm.getObject(TaskModel.TASK_RUNTIME);
		return r;
	}
	
	
	protected void browseHomeDirClicked() {
		File file = homeDir == null ? null : new File(homeDir);
		if (file != null && !file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, getShell());
		if (directory != null) {
			homeDir = directory.getAbsolutePath();
			homeDirText.setText(homeDir);
		}
	}

	protected static File getDirectory(File startingDirectory, Shell shell) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		}

		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}
		return null;
	}

	public void fillHomeDir(IRuntime rt) {
		if( rt.getLocation() == null ) {
			homeDir = getDefaultHomeDirectory(rt.getRuntimeType());
		} else {
			// old runtime, load from it
			homeDir = rt.getLocation().toOSString();
		}
		homeDirText.setText(homeDir);
		
		boolean isWC = rt instanceof IRuntimeWorkingCopy;
		if( isWC ) {
			((IRuntimeWorkingCopy)rt).setLocation(new Path(homeDir));
		} 
		homeDirText.setEnabled(isWC);
		homeDirButton.setEnabled(isWC);
	}
	
	protected String getDefaultHomeDirectory(IRuntimeType rtt) {
		return "";
	}

	public boolean isHomeValid() {
		if( homeDir == null  || homeDir.length() == 1 || !(new File(homeDir).exists())) 
			return false;
		ServerBeanLoader l = new ServerBeanLoader(new File(homeDir));
		if( l.getServerBeanType() == ServerBeanType.UNKNOWN) {
			return false;
		}
		return true;
	}
}
