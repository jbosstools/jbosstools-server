/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class JBoss7RuntimeWizardFragment extends JBossRuntimeWizardFragment {

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	protected void createWidgets(Composite main) {
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
		createConfigurationComposite(main);
	}

	protected void createConfigurationComposite(Composite main) {
		UIUtil u = new UIUtil(); // top bottom left right
		configComposite = new Composite(main, SWT.NONE);
		configComposite.setLayoutData(u.createFormData(
				jreComposite, 10, 100, -5, 0, 5, 100, -5));
		configComposite.setLayout(new FormLayout());
		
		configDirLabel = new Label(configComposite, SWT.NONE);
		configDirLabel.setText("Configuration file: ");
		configDirText = new Text(configComposite, SWT.BORDER);
		
		configBrowse = new Button(configComposite, SWT.NONE);
		configBrowse.setText(Messages.browse);
		
		// Organize them
		configDirLabel.setLayoutData(u.createFormData(
				0, 7, null, 0, 0, 5, null, 0));
		configDirText.setLayoutData(u.createFormData(
				0, 5, null, 0, configDirLabel, 5, configBrowse, -5));
		configBrowse.setLayoutData(u.createFormData(
				0, 5, null, 0, null, 0, 100, -5));
		
		configDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				configDirTextVal = configDirText.getText();
				updatePage();
			} 
		});
		
		configBrowse.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configBrowsePressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}
	
	protected void configBrowsePressed() {
		IPath f1 = null;
		if(new Path(configDirText.getText()).isAbsolute()) {
			f1 = new Path(configDirText.getText());
		} else {
			f1 = new Path(homeDir).append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
					.append(IJBossRuntimeResourceConstants.CONFIGURATION)
					.append(configDirText.getText());
		}
		String folder = f1.removeLastSegments(1).toString();
		File file = new File(folder);
		if (!file.exists()) {
			file = null;
		}

		File ffile = getFile(file, homeDirComposite.getShell());
		if (ffile != null) {
			IPath standaloneFolder = new Path(homeDir).append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
					.append(IJBossRuntimeResourceConstants.CONFIGURATION);
			if(ffile.getAbsolutePath().startsWith(standaloneFolder.toFile().getAbsolutePath())) {
				String result = ffile.getAbsolutePath().substring(standaloneFolder.toString().length());
				configDirText.setText(new Path(result).makeRelative().toString());
			} else {
				IPath ffilePath = new Path(ffile.getAbsolutePath());
				String relativeToStandalone = makeRelativeToStandaloneConfig(standaloneFolder, ffilePath);
				configDirText.setText(relativeToStandalone);
			}
		}
		configDirTextVal = configDirText.getText();
	}
	private String makeRelativeToStandaloneConfig(IPath standaloneFolder, IPath ffilePath) {
		StringBuffer sb = new StringBuffer();
		boolean done = false;
		while( !done ) {
			if( !standaloneFolder.isPrefixOf(ffilePath)) {
				sb.append("../");
				standaloneFolder = standaloneFolder.removeLastSegments(1);
			} else {
				done = true;
			}
		}
		sb.append(ffilePath.removeFirstSegments(standaloneFolder.segmentCount()).toString());
		return sb.toString();
	}

	protected static File getFile(File startingDirectory, Shell shell) {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
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
	
	
	protected void fillWidgets() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		
		if (rt != null) {
			try {
				fillNameWidgets(rt);
				fillHomeDir(rt);
				fillConfigWidgets(rt);
				fillJREWidgets(rt);
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, MessageFormat.format(Messages.JBoss7ServerWizardFragment_could_not_create_ui, rt.getName()), e);
				JBossServerUIPlugin.getDefault().getLog().log(status);
			}
		}
	}

	protected void fillConfigWidgets(IRuntime rt) {
		LocalJBoss7ServerRuntime rt2 = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		configDirText.setText(rt2.getConfigurationFile());
	}

	@Override
	protected void updatePage() {
		jreComboIndex = jreCombo.getSelectionIndex();
		int offset = -1;
		if( jreComboIndex + offset >= 0 )
			selectedVM = installedJREs.get(jreComboIndex + offset);
		else // if sel < 0 or sel == 0 and offset == -1
			selectedVM = null;
		configDirTextVal = configDirText.getText();
		updateErrorMessage();
		saveDetailsInRuntime();
	}
	
	@Override
	protected String getErrorString() {
		if (nameText == null)
			// not yet initialized. no errors
			return null;

		if (getRuntime(name) != null)
			return Messages.rwf_NameInUse;

		if (name == null || name.equals("")) //$NON-NLS-1$
			return Messages.rwf_nameTextBlank;
		

		if( getValidJREs().size() == 0 )
			return NLS.bind(Messages.rwf_noValidJRE, getRuntime().getExecutionEnvironment().getId());
		
		JBossExtendedProperties props = (JBossExtendedProperties)getRuntime().getRuntime().getAdapter(JBossExtendedProperties.class);
		if( props != null && props.requiresJDK() )  {
			if( jreComboIndex != 0 && selectedVM != null) {
				if( !JavaUtils.isJDK(selectedVM)) {
					// The chosen vm has no jdk
					return Messages.rwf_requiresJDK;
				}
			}
		}
		
		
		if( !homeDirectoryIsDirectory()) 
			return Messages.rwf_homeIsNotDirectory;
		
		if( !jbossModulesJarExists())
			return NLS.bind(Messages.rwf_homeMissingFiles2, getJBossModulesJar());
		
		if( configDirTextVal != null) {
			IPath p = new Path(configDirTextVal);
			if( p.isAbsolute() ) {
				return Messages.bind(Messages.rwf7_ConfigFileAbsoluteError, p.toString());
			}
			IPath actualPath = new Path(homeDir)
					.append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
					.append(IJBossRuntimeResourceConstants.CONFIGURATION).append(p);
			if( !actualPath.toFile().exists()) {
				return Messages.bind(Messages.rwf7_ConfigFileError, actualPath.toString());
			}
		}
		return null;
	}
	
	
	@Override
	public String getWarningString() {
		if (!systemJarExists())
			return NLS.bind(Messages.rwf_homeMissingFiles2, getSystemJarPath());
		JBossExtendedProperties props = (JBossExtendedProperties)getRuntime().getRuntime().getAdapter(JBossExtendedProperties.class);
		if( props != null && props.requiresJDK() )  {
			if( jreComboIndex == 0 || selectedVM == null) {
				return Messages.rwf_jdkWarning;
			}
		}
		// superclass handles the version warning
		return super.getWarningString();
	}

	protected boolean homeDirectoryIsDirectory() {
		if (homeDir == null || homeDir.length() == 0 )
			return false;
		File home = new File(homeDir);
		if( !home.exists() || !home.isDirectory()) {
			return false;
		}
		return true;
	}

	protected boolean jbossModulesJarExists() {
		return getJBossModulesJar().toFile().exists();
	}
	
	protected IPath getJBossModulesJar() {
		return new Path(homeDir).append("jboss-modules.jar");
	}
	
	
	
	protected boolean systemJarExists() {
		String s = getSystemJarPath();
		IPath p = new Path(homeDir).append(s);
		return p.toFile().exists();
	}
	
	@Override
	protected String getSystemJarPath() {
		return JBossServerType.AS7.getSystemJarPath();
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		exit();
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		((IRuntimeWorkingCopy) rt).setLocation(new Path(homeDir));
		saveRuntimeLocationInPreferences(rt);
	}

	
	@Override
	protected void saveConfigurationDetailsInRuntime(IRuntimeWorkingCopy wc) {
		LocalJBoss7ServerRuntime srt = (LocalJBoss7ServerRuntime) wc.loadAdapter(
				LocalJBoss7ServerRuntime.class, new NullProgressMonitor());
		if( configDirTextVal != null && !"".equals(configDirTextVal))
			srt.setConfigurationFile(configDirTextVal);
	}

}
