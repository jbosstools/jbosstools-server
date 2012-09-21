/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.wizards.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspacePreferenceManager;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.util.composites.ArchiveFilesetDestinationComposite;
import org.jboss.ide.eclipse.archives.ui.util.composites.ArchiveSourceDestinationComposite;
import org.jboss.ide.eclipse.archives.ui.util.composites.FilesetPreviewComposite;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class FilesetInfoWizardPage extends WizardPage {

	private IArchiveNode parentNode;
	private IArchiveStandardFileSet fileset;
	private String includes, excludes;
	private String projectName;
	private boolean flattened;

	/**
	 * This variable must at all times be global. ALWAYS
	 */
	private FilesetPreviewComposite previewComposite;

	private Composite mainComposite;
	private Label flattenedLabel;
	private ArchiveSourceDestinationComposite srcDestComposite;
	private Button flattenedYes;
	private Button flattenedNo;
	private Text includesText;
	private Text excludesText;
	private ArchiveFilesetDestinationComposite destinationComposite;
	private Composite flattenedEditor;

	public FilesetInfoWizardPage (Shell parent, IArchiveStandardFileSet fileset, IArchiveNode parentNode) {
		super(ArchivesUIMessages.FilesetInfoWizardPage_new_title, ArchivesUIMessages.FilesetInfoWizardPage_new_title, null);

		if (fileset == null) {
			setTitle(ArchivesUIMessages.FilesetInfoWizardPage_new_title);
			setMessage(ArchivesUIMessages.FilesetInfoWizardPage_new_message);
		} else {
			setTitle(ArchivesUIMessages.FilesetInfoWizardPage_edit_title);
			setMessage(ArchivesUIMessages.FilesetInfoWizardPage_edit_message);
		}

		this.fileset = fileset;
		this.parentNode = parentNode;
		projectName = parentNode.getProjectName();
	}

	public void createControl (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		mainComposite.setLayout(new GridLayout(1, false));
		Group info = createInfoGroup(mainComposite);
		createPreviewGroup(mainComposite, info);
		//mainComposite.layout();
		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				fillDefaults();
				addListeners();
				changePreview();
				includesText.setFocus();
			}
		});
		setControl(mainComposite);
	}

	private Group createPreviewGroup(Composite mainComposite, Group info) {
		Group previewGroup = new Group(mainComposite, SWT.NONE);
		previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		previewGroup.setLayout(new GridLayout());
		previewComposite = new FilesetPreviewComposite(previewGroup, SWT.NONE);
		previewComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		previewGroup.setText(ArchivesUIMessages.FilesetInfoWizardPage_previewGroup_label);

		return previewGroup;
	}

	private Group createInfoGroup(Composite mainComposite) {
		Group infoGroup = new Group(mainComposite, SWT.NONE);
		infoGroup.setText(ArchivesUIMessages.FilesetInfoWizardPage_infoGroup_title);
		infoGroup.setLayout(new GridLayout(3,false));
		infoGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		destinationComposite = new ArchiveFilesetDestinationComposite(infoGroup, SWT.NONE, parentNode);

		// root dir
		srcDestComposite = new ArchiveSourceDestinationComposite(ArchivesUIMessages.FilesetInfoWizardPage_rootDirectory_label, infoGroup, projectName, getDescriptorVersion());

		flattenedLabel = new Label(infoGroup, SWT.NONE);
		flattenedLabel.setLayoutData(new GridData(SWT.END,SWT.CENTER,false,false));
		new Label(infoGroup, SWT.NONE); // no icon for this field
		
		flattenedEditor = new Composite(infoGroup,SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 0;
		fillLayout.marginWidth = 0;
		flattenedEditor.setLayout(fillLayout);
		flattenedYes = new Button(flattenedEditor, SWT.RADIO);
		flattenedNo = new Button(flattenedEditor, SWT.RADIO);

		Label includesTextLabel = new Label(infoGroup, SWT.NONE);
		includesTextLabel.setLayoutData(new GridData(SWT.END,SWT.CENTER,false,false));
		Label includesImage = new Label(infoGroup, SWT.NONE);		
		includesText = new Text(infoGroup, SWT.BORDER);
		includesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label excludesTextLabel = new Label(infoGroup, SWT.NONE);
		excludesTextLabel.setLayoutData(new GridData(SWT.END,SWT.CENTER,false,false));
		Label excludesImage = new Label(infoGroup, SWT.NONE);
		excludesText = new Text(infoGroup, SWT.BORDER);
		excludesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// customize widgets
		includesImage.setImage(ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_INCLUDES));
		includesTextLabel.setText(ArchivesUIMessages.FilesetInfoWizardPage_includes_label);
		excludesImage.setImage(ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_EXCLUDES));
		excludesTextLabel.setText(ArchivesUIMessages.FilesetInfoWizardPage_excludes_label);

		flattenedLabel.setText(ArchivesUIMessages.Flatten);
		flattenedYes.setText(ArchivesUIMessages.Yes);
		flattenedNo.setText(ArchivesUIMessages.No);

		return infoGroup;
	}

	private void addListeners () {
		includesText.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				includes = includesText.getText();
				changePreview();
			}
		});

		excludesText.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				excludes = excludesText.getText();
				changePreview();
			}
		});

		SelectionAdapter flattenAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				flattened = flattenedYes.getSelection();
				changePreview();
			}
		};
		flattenedYes.addSelectionListener(flattenAdapter);
		flattenedNo.addSelectionListener(flattenAdapter);

		srcDestComposite.addChangeListener(new ArchiveSourceDestinationComposite.ChangeListener() {
			public void compositeChanged() {
				validate();
				changePreview();
			}
		});
	}

	private boolean validate () {
		String message = srcDestComposite.getMessage();
		int messageType = srcDestComposite.getStatusType();
		
		setMessage(message, messageType);
		setPageComplete(messageType <= IStatus.WARNING);
		return messageType <= IStatus.WARNING;
	}

	
	public IArchiveNode getRootNode () {
		return (IArchiveNode) destinationComposite.getPackageNodeDestination();
	}

	public String getIncludes () {
		return includes;
	}

	public String getExcludes () {
		return excludes;
	}

	public boolean isFlattened() {
		return flattened;
	}

	public boolean isRootDirWorkspaceRelative () {
		return srcDestComposite.isWorkspaceRelative();
	}

	public String getRawPath() {
		return srcDestComposite.getPath();
	}

	public String replaceVariables() {
		try {
			return ArchivesCore.getInstance().getVFS().
				performStringSubstitution(srcDestComposite.getPath(),
						projectName, true);
		} catch( CoreException ce ) {
		}
		return null;
	}

	private void fillDefaults () {
		if (fileset != null) {
			flattened = fileset.isFlattened();
			flattenedYes.setSelection(flattened);
			flattenedNo.setSelection(!flattened);

			if (fileset.getIncludesPattern() != null) {
				includes = fileset.getIncludesPattern();
				includesText.setText(includes);
			}
			if (fileset.getExcludesPattern() != null) {
				excludes = fileset.getExcludesPattern();
				excludesText.setText(excludes);
			}

			if (fileset.getRawSourcePath() != null) {
				srcDestComposite.init(fileset.getRawSourcePath(), fileset.isInWorkspace());
			}

		} else {
			String rawPath = ""; //$NON-NLS-1$
			srcDestComposite.init(rawPath, true);
			flattened = false;
			flattenedYes.setSelection(flattened);
			flattenedNo.setSelection(!flattened);
			includes = "**"; //$NON-NLS-1$
			includesText.setText(includes);
			boolean useDefaultExcludes = PrefsInitializer.getBoolean(PrefsInitializer.PREF_USE_DEFAULT_EXCLUDES);
			if( useDefaultExcludes && parentNode != null)
				excludesText.setText(
						PrefsInitializer.getString(
								PrefsInitializer.PREF_DEFAULT_EXCLUDE_LIST, 
								WorkspacePreferenceManager.getResource(
										parentNode.getProjectPath()), 
								true));
		}

	}

	private ChangePreviewRunnable changePreviewRunnable;
	private void changePreview() {
		if( changePreviewRunnable != null )
			changePreviewRunnable.stop = true;
		changePreviewRunnable = new ChangePreviewRunnable();
		Thread t = new Thread(changePreviewRunnable);
		t.start();
	}

	protected class ChangePreviewRunnable implements Runnable {
		public boolean stop = false;
		public void run() {
			DirectoryScannerExtension ds = null;
			Runnable r;
			try {
				String effectiveExcludes = excludes;
				if( parentNode.getRootArchive().isDestinationInWorkspace()) {
					effectiveExcludes += "," + parentNode.getRootArchive().getRawDestinationPath(); //$NON-NLS-1$
				}
				System.out.println(6);
				IPath parentRelativeToRoot = parentNode.getRootArchiveRelativePath();
				ds = DirectoryScannerFactory.createDirectoryScanner( 
						replaceVariables(), parentRelativeToRoot, includes, effectiveExcludes, parentNode.getProjectName(),
						srcDestComposite.isWorkspaceRelative(), parentNode.getModelRootNode().getDescriptorVersion(), false);
				Iterator<File> it = ds.iterator();
				ArrayList<String> paths2 = new ArrayList<String>();
				while(it.hasNext() && paths2.size() < 30) {
					FileWrapper fw = (FileWrapper)it.next();
					paths2.add(fw.getFilesetRelative());
				}
				IPath filesetRelative;
				final ArrayList<IPath> list = new ArrayList<IPath>();
				for( int i = 0; i < paths2.size(); i++ ) {
					if( flattened )
						filesetRelative = new Path(new Path(paths2.get(i)).lastSegment());
					else
						filesetRelative = new Path(paths2.get(i));
					if( !list.contains(filesetRelative))
						list.add(filesetRelative);
				}
				r = new Runnable() {
					public void run() {
						previewComposite.setInput(list.toArray());
					}
				};
			} catch( Exception e ) {
				e.printStackTrace(); 
				r = new Runnable() {
					public void run() {
						previewComposite.setInput(new IPath[0]);
					}
				};
			}

			if( !stop ) {
				Display.getDefault().asyncExec(r);
			}
		}
	}

	protected double getDescriptorVersion() {
		return parentNode.getModelRootNode().getDescriptorVersion();
	}
}
