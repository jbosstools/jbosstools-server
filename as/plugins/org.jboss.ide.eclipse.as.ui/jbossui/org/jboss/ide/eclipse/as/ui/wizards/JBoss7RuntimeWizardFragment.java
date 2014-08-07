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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
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
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class JBoss7RuntimeWizardFragment extends JBossRuntimeWizardFragment {
	private Label baseDirLabel;
	private Text baseDirText;
	private Button baseDirBrowse;
	private String baseDirTextVal;

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

		
		baseDirLabel = new Label(configComposite, SWT.NONE);
		baseDirLabel.setText("Configuration base directory: ");
		baseDirText = new Text(configComposite, SWT.BORDER);
		
		baseDirBrowse = new Button(configComposite, SWT.NONE);
		baseDirBrowse.setText(Messages.browse);

		
		// Organize them
		baseDirLabel.setLayoutData(u.createFormData(
				0, 7, null, 0, 0, 5, null, 0));
		baseDirText.setLayoutData(u.createFormData(
				0, 5, null, 0, baseDirLabel, 5, baseDirBrowse, -5));
		baseDirBrowse.setLayoutData(u.createFormData(
				0, 5, null, 0, null, 0, 100, -5));
		
		configDirLabel.setLayoutData(u.createFormData(
				baseDirText, 7, null, 0, 0, 5, null, 0));
		configDirText.setLayoutData(u.createFormData(
				baseDirText, 5, null, 0, configDirLabel, 5, configBrowse, -5));
		configBrowse.setLayoutData(u.createFormData(
				baseDirText, 5, null, 0, null, 0, 100, -5));
		
		
		configDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				configDirTextVal = configDirText.getText();
				updatePage();
			} 
		});

		
		baseDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				baseDirTextVal = baseDirText.getText();
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

		baseDirBrowse.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				baseDirBrowsePressed();
				updatePage();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	public void updateModels() {
		super.updateModels();
		IJBossServerRuntime rt = getRuntime();
		if( rt.getRuntime().getLocation() != null ) {
			LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)rt;
			String bd = jb7rt.getBaseDirectory();
			IPath relative = ServerUtil.makeRelative(rt.getRuntime(), new Path(bd));
			baseDirTextVal = relative.toString();
		}
	}
	
	
	private File getConfigBrowseInitialFolder() {
		String homeDir = homeDirComposite.getHomeDirectory();
		IPath f1 = null;
		if(new Path(configDirTextVal).isAbsolute()) {
			// If the configuration file (not dir, despite superclass var name) 
			// is absolute, then just use that
			f1 = new Path(configDirTextVal);
		} else {
			// Else, 
			f1 = new Path(getAbsoluteBaseDir(baseDirTextVal, homeDir))
					.append(IJBossRuntimeResourceConstants.CONFIGURATION)
					.append(configDirTextVal);
		}
		String folder = f1.removeLastSegments(1).toString();
		File file = new File(folder);
		if (!file.exists()) {
			file = null;
		}
		
		return file;
	}
	
	protected void configBrowsePressed() {
		String homeDir = homeDirComposite.getHomeDirectory();
		File file = getConfigBrowseInitialFolder();
		File ffile = getFile(file, homeDirComposite.getShell());
		if (ffile != null) {
			// Chosen basedir's config folder
			IPath chosenConfigFolder = new Path(getAbsoluteBaseDir(baseDirTextVal, homeDir))
					.append(IJBossRuntimeResourceConstants.CONFIGURATION);
			
			if(ffile.getAbsolutePath().startsWith(chosenConfigFolder.toFile().getAbsolutePath())) {
				String result = ffile.getAbsolutePath().substring(chosenConfigFolder.toString().length());
				configDirTextVal = (new Path(result).makeRelative().toString());
			} else {
				IPath ffilePath = new Path(ffile.getAbsolutePath());
				String relativeToConfig = makeRelativeToConfigFolder(chosenConfigFolder, ffilePath);
				configDirTextVal = (relativeToConfig);
			}
			configDirText.setText(configDirTextVal);
		}
	}
	
	/**
	 * @since 3.0
	 */
	protected void baseDirBrowsePressed() {
		String homeDir = homeDirComposite.getHomeDirectory();
		File file = null;
		if( new Path(baseDirTextVal).isAbsolute()) {
			file = new Path(baseDirTextVal).toFile();
		} else {
			file = new Path(homeDir).append(baseDirTextVal).toFile();
		}
		File ffile = getDirectory(file, homeDirComposite.getShell());
		if (ffile != null) {
			IPath ffilePath = new Path(ffile.getAbsolutePath());
			if( new Path(homeDir).isPrefixOf(ffilePath)) {
				String relative = ffile.getAbsolutePath().substring(homeDir.toString().length());
				baseDirTextVal = new Path(relative).makeRelative().toString();
			} else {
				baseDirTextVal = ffile.getAbsolutePath();
			}
			baseDirText.setText(baseDirTextVal);
		}
	}
	
	
	
	/**
	 * Given a baseFolder folder of /home/user/jboss7/standalone2
	 * and a config-file of /home/user/jboss7/standalone4/mystandalone.xml, 
	 * this should return ../standalone4/mystandalone.xml
	 * 
	 * @param baseFolder  the base folder for your configuration
	 * @param ffilePath   the absolute file-system location of the chosen config xml
	 * @return  a relative path between the two
	 */
	private String makeRelativeToConfigFolder(IPath baseFolder, IPath ffilePath) {
		StringBuffer sb = new StringBuffer();
		boolean done = false;
		while( !done ) {
			if( !baseFolder.isPrefixOf(ffilePath)) {
				sb.append("../");
				baseFolder = baseFolder.removeLastSegments(1);
			} else {
				done = true;
			}
		}
		sb.append(ffilePath.removeFirstSegments(baseFolder.segmentCount()).toString());
		return sb.toString();
	}

	// Open a file-chooser dialog
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
	
	@Override
	protected void fillConfigWidgets(IRuntime rt) {
		LocalJBoss7ServerRuntime rt2 = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		baseDirTextVal = getPresentableBaseDir(rt);
		baseDirText.setText(baseDirTextVal);
		configDirTextVal = rt2.getConfigurationFile();
		configDirText.setText(configDirTextVal);
	}

	/**
	 * Returns a presentable string representing the basedir. 
	 * For basedir that is relative to server home, such as 'standalone', 
	 * only 'standalone' will be returned, whereas for base directories 
	 * outside of the tree, a file-system full path should be returned
	 * 
	 * @param rt
	 * @return
	 */
	private String getPresentableBaseDir(IRuntime rt) {
		LocalJBoss7ServerRuntime rt2 = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		String baseDir = rt2.getBaseDirectory();
		IPath baseDirPath = new Path(baseDir);
		if( baseDirPath.isAbsolute()) {
			IPath rtLocWithSep = rt.getLocation().addTrailingSeparator(); 
			if( rtLocWithSep.isPrefixOf(new Path(baseDir))) {
				return baseDir.substring(rtLocWithSep.toOSString().length());
			}
		}
		return baseDir;
	}
	
	/**
	 * Get the base directory as an absolute file-system path
	 * @param path
	 * @param rt
	 * @return
	 */
	private String getAbsoluteBaseDir(String path, String homeDir) {
		if( new Path(path).isAbsolute())
			return path;
		return new Path(homeDir).append(path).toString();
	}
	
	
	@Override
	protected void updatePage() {
		configDirTextVal = configDirText.getText();
		saveDetailsInRuntime();
		updateErrorMessage();
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
		

		if( jreComposite != null ) {
			IExecutionEnvironment selectedEnv = jreComposite.getSelectedExecutionEnvironment();
			IVMInstall install = jreComposite.getSelectedVM();
			if( install == null ) {
				// user has selected an exec-env, not a vm
				if( selectedEnv != null ) {
					if( selectedEnv.getCompatibleVMs().length == 0 ) {
						return NLS.bind(Messages.rwf_noValidJRE, selectedEnv.getId());
					}
				}
			}
		}
		
		if( jreComposite != null && jreComposite.getValidJREs().size() == 0 )
			return NLS.bind(Messages.rwf_noValidJRE, getRuntime().getExecutionEnvironment().getId());
		
		if( !homeDirectoryIsDirectory()) 
			return Messages.rwf_homeIsNotDirectory;
		
		if( !jbossModulesJarExists())
			return NLS.bind(Messages.rwf_homeMissingFiles2, getJBossModulesJar());
		
		if( configDirTextVal != null) {
			String homeDir = homeDirComposite.getHomeDirectory();
			IPath p = new Path(configDirTextVal);
			if( p.isAbsolute() ) {
				return Messages.bind(Messages.rwf7_ConfigFileAbsoluteError, p.toString());
			}
			IPath actualPath = baseDirTextVal == null ? null : 
					new Path(getAbsoluteBaseDir(baseDirTextVal, homeDir))
					.append(IJBossRuntimeResourceConstants.CONFIGURATION).append(p);
			if( actualPath == null || !actualPath.toFile().exists()) {
				return Messages.bind(Messages.rwf7_ConfigFileError, actualPath == null ? null : actualPath.toString());
			}
		}
		return null;
	}
	
	
	@Override
	public String getWarningString() {
		JBossExtendedProperties props = (JBossExtendedProperties)getRuntime().getRuntime().getAdapter(JBossExtendedProperties.class);
		if( props != null && props.requiresJDK() )  {
			IVMInstall selected = jreComposite.getSelectedVM();
			if( selected == null ) {
				// We have a null selected vm, so check effective vm
				boolean isjdk = JavaUtils.isJDK(getRuntime().getVM());
				if( !isjdk) {
					return Messages.rwf_jdkWarning;
				}
			} else if( !JavaUtils.isJDK(selected)) {
				// The chosen vm has no jdk
				return Messages.rwf_requiresJDK;
			}
		}
		
		// superclass handles the version warning
		return super.getWarningString();
	}

	protected boolean homeDirectoryIsDirectory() {
		String homeDir = homeDirComposite.getHomeDirectory();
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
		String homeDir = homeDirComposite.getHomeDirectory();
		return new Path(homeDir).append("jboss-modules.jar");
	}
	
	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		exit();
		String homeDir = homeDirComposite.getHomeDirectory();
		IRuntime rt = getRuntimeFromTaskModel();
		((IRuntimeWorkingCopy) rt).setLocation(new Path(homeDir));
		saveRuntimeLocationInPreferences(rt);
	}

	
	@Override
	protected void saveConfigurationDetailsInRuntime(IRuntimeWorkingCopy wc) {
		LocalJBoss7ServerRuntime srt = (LocalJBoss7ServerRuntime) wc.loadAdapter(
				LocalJBoss7ServerRuntime.class, new NullProgressMonitor());
		if( configDirTextVal != null && !"".equals(configDirTextVal))
			srt.setConfigurationFile(configDirTextVal);
		if( baseDirTextVal != null && !"".equals(baseDirTextVal))
			srt.setBaseDirectory(baseDirTextVal);
	}

}
