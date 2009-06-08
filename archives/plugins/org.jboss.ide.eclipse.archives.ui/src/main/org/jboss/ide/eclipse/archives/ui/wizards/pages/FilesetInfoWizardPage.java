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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
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
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		mainComposite.setLayout(new FormLayout());
		Composite restrainer = new Composite(mainComposite, SWT.NONE);
		restrainer.setLayout(new FormLayout());
		FormData restrainerData = new FormData();
		restrainerData.left = new FormAttachment(0,5);
		restrainerData.top = new FormAttachment(0,5);
		restrainerData.bottom = new FormAttachment(100,-5);
		restrainerData.right = new FormAttachment(0,600);
		restrainer.setLayoutData(restrainerData);
		
		Group info = createInfoGroup(restrainer);
		createPreviewGroup(restrainer, info);
		mainComposite.layout();
		fillDefaults();
		addListeners();
		changePreview();

		includesText.setFocus();

		setControl(mainComposite);
	}

	private FormData createFormData(Object topStart, int topOffset, Object bottomStart, int bottomOffset,
			Object leftStart, int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if( topStart != null ) {
			data.top = topStart instanceof Control ? new FormAttachment((Control)topStart, topOffset) :
				new FormAttachment(((Integer)topStart).intValue(), topOffset);
		}

		if( bottomStart != null ) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment((Control)bottomStart, bottomOffset) :
				new FormAttachment(((Integer)bottomStart).intValue(), bottomOffset);
		}

		if( leftStart != null ) {
			data.left = leftStart instanceof Control ? new FormAttachment((Control)leftStart, leftOffset) :
				new FormAttachment(((Integer)leftStart).intValue(), leftOffset);
		}

		if( rightStart != null ) {
			data.right = rightStart instanceof Control ? new FormAttachment((Control)rightStart, rightOffset) :
				new FormAttachment(((Integer)rightStart).intValue(), rightOffset);
		}

		return data;
	}


	private Group createPreviewGroup(Composite mainComposite, Group info) {
		Group previewGroup = new Group(mainComposite, SWT.NONE);
		previewGroup.setLayoutData(createFormData(info,5,100,-5,0,5,100,-5));
		previewGroup.setLayout(new FormLayout());
		Label invisibleLabel = new Label(previewGroup, SWT.NONE);
		invisibleLabel.setLayoutData(createFormData(0,0,0,200,0,0,0,1));
		previewComposite = new FilesetPreviewComposite(previewGroup, SWT.NONE);
		previewComposite.setLayoutData(createFormData(0,0,100,0,0,0,100,0));
		previewGroup.setText(ArchivesUIMessages.FilesetInfoWizardPage_previewGroup_label);
		return previewGroup;
	}

	private Group createInfoGroup(Composite mainComposite) {
		Group infoGroup = new Group(mainComposite, SWT.NONE);
		infoGroup.setText(ArchivesUIMessages.FilesetInfoWizardPage_infoGroup_title);

		// positioning in parent
		infoGroup.setLayoutData(createFormData(0,5,null,0,0,5,100,-5));

		// my layout
		infoGroup.setLayout(new FormLayout());

		// destination row
		Label destinationKey = new Label(infoGroup, SWT.NONE);
		destinationComposite = new ArchiveFilesetDestinationComposite(infoGroup, SWT.NONE, parentNode);

		destinationKey.setLayoutData(createFormData(0,10,null,0,null,5, 0, 100));
		destinationComposite.setLayoutData(createFormData(0,5,null,0,destinationKey,5, 100, -5));

		// root dir
		Label rootDirectoryLabel = new Label(infoGroup, SWT.NONE);
		srcDestComposite = new ArchiveSourceDestinationComposite(infoGroup, projectName, getDescriptorVersion());
		Composite rootDirValue = srcDestComposite;
		rootDirectoryLabel.setLayoutData(createFormData(destinationComposite,10,null,0,null,5,0,100));
		rootDirValue.setLayoutData(createFormData(destinationComposite,5,null,0,rootDirectoryLabel,5,100,-5));

		flattenedLabel = new Label(infoGroup, SWT.NONE);
		flattenedYes = new Button(infoGroup, SWT.RADIO);
		flattenedNo = new Button(infoGroup, SWT.RADIO);
		flattenedLabel.setLayoutData(createFormData(rootDirValue,5,null,0,null,0,rootDirValue,-5));
		flattenedYes.setLayoutData(createFormData(rootDirValue, 5, null,0,flattenedLabel,5,null,0));
		flattenedNo.setLayoutData(createFormData(rootDirValue, 5, null,0,flattenedYes,5,null,0));

		// includes composite and it's internals
		Composite includesKey = new Composite(infoGroup, SWT.NONE);
		includesKey.setLayout(new FormLayout());
		Label includesImage = new Label(includesKey, SWT.NONE);
		Label includesTextLabel = new Label(includesKey, SWT.NONE);
		includesText = new Text(infoGroup, SWT.BORDER);
		includesImage.setLayoutData(createFormData(0,0,null,0,0,0,null,0));
		includesTextLabel.setLayoutData(createFormData(0,0,null,0,includesImage,5,null,0));

		includesKey.setLayoutData(createFormData(flattenedLabel,5,null,0,null,5,0,100));
		includesText.setLayoutData(createFormData(flattenedLabel,5,null,0,includesKey,10,100,-5));


		// excludes composite and it's internals
		Composite excludesKey = new Composite(infoGroup, SWT.NONE);
		excludesKey.setLayout(new FormLayout());
		Label excludesImage = new Label(excludesKey, SWT.NONE);
		Label excludesTextLabel = new Label(excludesKey, SWT.NONE);
		excludesText = new Text(infoGroup, SWT.BORDER);
		excludesImage.setLayoutData(createFormData(0,0,null,0,0,0,null,0));
		excludesTextLabel.setLayoutData(createFormData(0,0,null,0,excludesImage,5,null,0));

		excludesKey.setLayoutData(createFormData(includesText,5,null,0,null,5,0,100));
		excludesText.setLayoutData(createFormData(includesText,5,100,-5,excludesKey,10,100,-5));

		// customize widgets
		destinationKey.setText(ArchivesUIMessages.FilesetInfoWizardPage_destination_label);
		rootDirectoryLabel.setText(ArchivesUIMessages.FilesetInfoWizardPage_rootDirectory_label);
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
				ds = DirectoryScannerFactory.createDirectoryScanner(
						replaceVariables(), null, includes, excludes, parentNode.getProjectName(),
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
