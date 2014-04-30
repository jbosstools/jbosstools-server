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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

/**
 * @author Stryker
 */
public class JBossRuntimeWizardFragment extends AbstractJBTRuntimeWizardFragment {

	// Configuration stuff
	protected Composite configComposite;
	protected Group configGroup;
	protected Label configDirLabel;
	protected Text configDirText;
	protected JBossConfigurationTableViewer configurations;
	protected Button configCopy, configBrowse, configDelete;
	protected String configDirTextVal;

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		return super.createComposite(parent, handle);
	}
	
	protected void updateWizardHandle(Composite parent) {
		// make modifications to parent
		IRuntime r = getRuntimeFromTaskModel();
		handle.setTitle( Messages.rwf_JBossRuntime);
		String descript = r.getRuntimeType().getDescription();
		handle.setDescription(descript);
		handle.setImageDescriptor(getImageDescriptor());
		initiateHelp(parent);
	}

	protected ImageDescriptor getImageDescriptor() {
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}

	protected void initiateHelp(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.jboss.ide.eclipse.as.doc.user.new_server_runtime"); //$NON-NLS-1$		
	}
	
	protected void createWidgets(Composite main) {
		super.createWidgets(main);
		createConfigurationComposite(main);
	}
	
	protected void fillWidgets(IRuntime rt) {
		super.fillWidgets(rt);
		fillConfigWidgets(rt);
	}
	
	protected void fillConfigWidgets(IRuntime rt) {
		IJBossServerRuntime jbsrt = getRuntime();
		String dirText = jbsrt.getConfigLocation();
		configDirText.setText(isEmpty(dirText) ? IConstants.SERVER : dirText);
		configurations.setConfiguration(isEmpty(jbsrt.getJBossConfiguration())
				? IConstants.DEFAULT_CONFIGURATION : jbsrt.getJBossConfiguration());
		configurations.getTable().setVisible(true);
	}
	
	@Override
	protected void saveJreInRuntime(IRuntimeWorkingCopy wc) {
		IJBossServerRuntime srt = (IJBossServerRuntime) wc.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());
		if( srt != null ) {
			if( jreComposite.getSelectedVM() != null )
				srt.setVM(jreComposite.getSelectedVM());
			else
				srt.setVM(null);
		}
	}
	
	@Override
	protected String getExplanationText() {
		return Messages.rwf_Explanation;
	}

	protected IJBossServerRuntime getRuntime() {
		IRuntime r = getRuntimeFromTaskModel();
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}
	
	protected void createConfigurationComposite(Composite main) {
		UIUtil u = new UIUtil(); // top bottom left right
		configComposite = new Composite(main, SWT.NONE);
		configComposite.setLayoutData(u.createFormData(
				jreComposite, 10, 100, -5, 0, 5, 100, -5));
		configComposite.setLayout(new FormLayout());
		
		configGroup = new Group(configComposite, SWT.DEFAULT);
		configGroup.setText(Messages.wf_ConfigLabel);
		configGroup.setLayoutData(u.createFormData(
				0, 0, 100, 0, 0, 0, 100, 0));
		configGroup.setLayout(new FormLayout());
		
		configDirLabel = new Label(configGroup, SWT.NONE);
		configDirLabel.setText(Messages.directory);
		configDirText = new Text(configGroup, SWT.BORDER);

		configurations = new JBossConfigurationTableViewer(configGroup,
		SWT.BORDER | SWT.SINGLE);
		
		IJBossServerRuntime srt = getRuntime();
		if( srt != null && !isEmpty(srt.getJBossConfiguration())) 
			configurations.setConfiguration(srt.getJBossConfiguration());
		
		configBrowse = new Button(configGroup, SWT.DEFAULT);
		configCopy = new Button(configGroup, SWT.DEFAULT);
		configDelete = new Button(configGroup, SWT.DEFAULT);
		configBrowse.setText(Messages.browse);
		configCopy.setText(Messages.copy);
		configDelete.setText(Messages.delete);
		
		// Organize them
		configDirLabel.setLayoutData(u.createFormData(
				2, 5, null, 0, 0, 5, null, 0));
		configDirText.setLayoutData(u.createFormData(
				0, 5, null, 0, configDirLabel, 5, configBrowse, -5));
		configBrowse.setLayoutData(u.createFormData(
				0, 5, null, 0, configurations.getTable(), 5, 100, -5));
		configurations.getTable().setLayoutData(u.createFormData(
				configDirText, 5, 100,-5, 0,5, 80, 0));
		configCopy.setLayoutData(u.createFormData(
				configBrowse, 5, null, 0, configurations.getTable(), 5, 100, -5));
		configDelete.setLayoutData(u.createFormData(
				configCopy, 5, null, 0, configurations.getTable(), 5, 100, -5));
		
		configDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
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

		configCopy.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configCopyPressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		configDelete.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configDeletePressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		configurations.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				updateErrorMessage();
				configDelete.setEnabled(!((IStructuredSelection)configurations.getSelection()).isEmpty());
				configCopy.setEnabled(!((IStructuredSelection)configurations.getSelection()).isEmpty());
			}
		});
	}
	
	protected void configBrowsePressed() {
		String homeDir = homeDirComposite.getHomeDirectory();
		String folder = new Path(configDirText.getText()).isAbsolute() ? 
				configDirText.getText() : new Path(homeDir).append(configDirText.getText()).toString();
		File file = new File(folder);
		if (!file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, homeDirComposite.getShell());
		if (directory != null) {
			if(directory.getAbsolutePath().startsWith(new Path(homeDir).toString())) {
				String result = directory.getAbsolutePath().substring(homeDir.length());
				configDirText.setText(new Path(result).makeRelative().toString());
			} else {
				configDirText.setText(directory.getAbsolutePath());
			}
		}
	}
	protected void configCopyPressed() {
		String homeDir = homeDirComposite.getHomeDirectory();
		CopyConfigurationDialog d = new CopyConfigurationDialog(configCopy.getShell(), homeDir, configDirText.getText(), configurations.getCurrentlySelectedConfiguration());
		if(d.open() == 0 ) {
			IPath source = new Path(configDirText.getText());
			if( !source.isAbsolute())
				source = new Path(homeDir).append(source);
			source = source.append(configurations.getCurrentlySelectedConfiguration());
			
			IPath dest = new Path(d.getNewDest());
			if( !dest.isAbsolute())
				dest = new Path(homeDir).append(dest);
			dest = dest.append(d.getNewConfig());
			dest.toFile().mkdirs();
			FileUtil.copyDir(source.toFile(), dest.toFile());
			configDirText.setText(d.getNewDest());
			configurations.setSelection(new StructuredSelection(d.getNewConfig()));
		}
		
	}
	protected void configDeletePressed() {
		String homeDir = homeDirComposite.getHomeDirectory();
        MessageDialog dialog = new MessageDialog(configBrowse.getShell(), 
        		Messages.JBossRuntimeWizardFragment_DeleteConfigTitle, null,
        		Messages.JBossRuntimeWizardFragment_DeleteConfigConfirmation, 
                MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0); // yes is the default
        if(dialog.open() == 0) {
        	String config = configurations.getCurrentlySelectedConfiguration();
        	String configDir = configDirText.getText();
        	File folder;
        	if( !new Path(configDir).isAbsolute())
        		folder = new Path(homeDir).append(configDir).append(config).toFile();
        	else
        		folder = new Path(configDir).append(config).toFile();
        	 
        	FileUtil.completeDelete(folder);
        	configurations.refresh();
        	updatePage();
        }
	}

	// Launchable only from UI thread
	protected void updateDependentWidgets() {
		super.updateDependentWidgets();
		String homeDir = homeDirComposite.getHomeDirectory();
		String folder;
		if (!homeDirComposite.isHomeValid()) {
			configurations.getControl().setEnabled(false);
			folder = homeDir;
		} else {
			IPath p = new Path(configDirText.getText());
			if( p.isAbsolute()) 
				folder = p.toString();
			else 
				folder = new Path(homeDir).append(p).toString();
		}
		configurations.setFolder(folder);
		File f = new File(folder);
		configurations.getControl().setEnabled(f.exists() && f.isDirectory());
		configDirTextVal = configDirText.getText();
	}

	protected String getErrorString() {
		String sup = super.getErrorString();
		if( sup == null ) {
			if( configurations.getSelection().isEmpty())
				return Messages.JBossRuntimeWizardFragment_MustSelectValidConfig;
		}
		return sup;
	}
	
	protected String getHomeVersionWarning() {
		String homeDir = homeDirComposite.getHomeDirectory();
		File loc = new File(homeDir);
		String serverId = new ServerBeanLoader(loc).getServerAdapterId();
		String rtId = serverId == null ? null : 
				ServerCore.findServerType(serverId).getRuntimeType().getId();
		IRuntime adapterRt = getRuntimeFromTaskModel();
		String adapterRuntimeId = adapterRt.getRuntimeType().getId();
		if( !adapterRuntimeId.equals(rtId)) {
			return NLS.bind(Messages.rwf_homeIncorrectVersionError, 
					adapterRt.getRuntimeType().getVersion(), 
					getVersionString(loc));
		}
		return null;
	}
	
	protected void saveDetailsInRuntime(IRuntimeWorkingCopy wc) {
		super.saveDetailsInRuntime(wc);
		saveConfigurationDetailsInRuntime(wc);
	}
	
	protected void saveConfigurationDetailsInRuntime(IRuntimeWorkingCopy wc) {
		IJBossServerRuntime srt = (IJBossServerRuntime) wc.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());
		if( configurations != null && configurations.getSelectedConfiguration() != null )
			srt.setJBossConfiguration(configurations.getSelectedConfiguration());
		if( configDirText != null )
			srt.setConfigLocation(configDirTextVal);
	}
}
