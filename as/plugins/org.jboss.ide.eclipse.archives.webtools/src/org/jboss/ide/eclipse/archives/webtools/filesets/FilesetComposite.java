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
package org.jboss.ide.eclipse.archives.webtools.filesets;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.ui.util.composites.FilesetPreviewComposite;
import org.jboss.ide.eclipse.archives.webtools.Messages;

public class FilesetComposite extends Composite {
	
	public static interface IFilesetCompositeErrorDisplay {
		public void updateError(String msg);
	}
	
	private IFilesetCompositeErrorDisplay errorDisplay;
	protected Fileset fileset;
	private String name, dir, includes, excludes;
	private Button browse;
	private Text includesText, excludesText, folderText, nameText;
	private FilesetPreviewComposite preview;
	private IServer server;
	private boolean showViewer = true;
	
	private boolean requiresName = true;

	public FilesetComposite(Composite parent, Fileset fileset, boolean showViewer) {
		this(parent, fileset, showViewer, true);
	}
	public FilesetComposite(Composite parent, Fileset fileset, boolean showViewer, boolean requiresName) {
		super(parent, SWT.None);
		this.fileset = (Fileset)fileset.clone();
		this.server = fileset.getServer();
		this.showViewer = showViewer;
		this.requiresName = requiresName;
		createArea(parent);
	}
	
	public void setErrorDisplay(IFilesetCompositeErrorDisplay d) {
		this.errorDisplay = d;
	}
	
	protected String getDefaultIncludesPattern() {
		return "**/*.xml"; //$NON-NLS-1$
	}
	
	public void setShowViewer(boolean val) {
		showViewer = val;
	}
	
	protected void createArea(Composite parent) {
		setLayout(new GridLayout(3, false));
		fillArea(this);
		
		if( requiresName )
			nameText.setText(fileset.getName());
		folderText.setText(fileset.getRawFolder());
		includesText.setText(fileset.getIncludesPattern());
		excludesText.setText(fileset.getExcludesPattern());

		addListeners();
		getShell().layout();
		textModified();
		updatePreview();
	}

	protected void addListeners() {
		ModifyListener mListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textModified();
			}
		};
		if( requiresName )
			nameText.addModifyListener(mListener);
		folderText.addModifyListener(mListener);
		includesText.addModifyListener(mListener);
		excludesText.addModifyListener(mListener);

		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				String txt = folderText.getText();
				if( !new Path(txt).isAbsolute() && server != null && server.getRuntime() != null)
					txt = server.getRuntime().getLocation().append(txt).toString();
				d.setFilterPath(txt);
				String x = d.open();
				if( x != null ) {
					folderText.setText(makeRelative(x));
				}
			}
		});
	}

	protected String makeRelative(String path) {
		if( server != null && server.getRuntime() != null ) {
			if( server.getRuntime().getLocation().isPrefixOf(new Path(path))) {
				String p2 = path.substring(server.getRuntime().getLocation().toString().length());
				return new Path(p2).makeRelative().toString();
			}
		}
		return path;
	}
	
	protected void textModified() {
		if( requiresName )
			name = nameText.getText();
		dir = folderText.getText();
		includes = includesText.getText();
		excludes = excludesText.getText();
		fileset.setName(name);
		boolean requiresPreviewUpdate = false;
		requiresPreviewUpdate |= (dir != null && !dir.equals(fileset.getRawFolder()));
		requiresPreviewUpdate |= (includes != null && !includes.equals(fileset.getIncludesPattern()));
		requiresPreviewUpdate |= (excludes != null && !excludes.equals(fileset.getExcludesPattern()));

		fileset.setFolder(dir);
		fileset.setIncludesPattern(includes);
		fileset.setExcludesPattern(excludes);
		if( requiresPreviewUpdate)
			updatePreview();
		if( errorDisplay != null ) {
			errorDisplay.updateError(getValidationError());
		}
	}
	
	
	public String getValidationError() {
		String error = null;
		if( requiresName && name.equals("")) //$NON-NLS-1$
			error = Messages.FilesetsDialogEmptyName;
		else if( dir.equals("")) //$NON-NLS-1$
			error = Messages.FilesetsDialogEmptyFolder;
		else
			error = null;
		return error;
	}

	protected void fillArea(Composite main) {
		
		if( requiresName ) {
			Label nameLabel = new Label(main, SWT.NONE);
			nameLabel.setText(Messages.FilesetsNewName);
			nameText = new Text(main, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		
		Label folderLabel = new Label(main, SWT.NONE);
		folderLabel.setText(Messages.FilesetsNewRootDir);

		folderText = new Text(main, SWT.BORDER);
		folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		browse = new Button(main, SWT.PUSH);
		browse.setText(Messages.FilesetsNewBrowse);

		Label includesLabel = new Label(main, SWT.NONE);
		includesLabel.setText(Messages.FilesetsNewIncludes);

		includesText = new Text(main, SWT.BORDER);
		includesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label excludeLabel= new Label(main, SWT.NONE);
		excludeLabel.setText(Messages.FilesetsNewExcludes);

		excludesText = new Text(main, SWT.BORDER);
		excludesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		if( showViewer ) {
			Group previewWrapper = new Group(main, SWT.NONE);
	
			previewWrapper.setLayout(new GridLayout());
	
			GridData data = new GridData(GridData.FILL_BOTH);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			data.horizontalSpan = 3;
			data.minimumHeight = 200;
	
			previewWrapper.setLayoutData(data);
			previewWrapper.setText(Messages.FilesetsNewPreview);
	
			previewWrapper.setLayout(new FillLayout());
			preview = new FilesetPreviewComposite(previewWrapper, SWT.NONE);
		}
	}

	private void updatePreview() {
		if( preview != null ) { 
			String resInc = fileset.getResolvedIncludesPattern();
			String resExc = fileset.getResolvedExclude();
			launchPreviewJob(fileset.getFolder(), resInc, resExc);
		}
	}
	
	private Job loadPreviewJob;
	private synchronized void launchPreviewJob(final String folder, final String inc, final String exc) {
		if( loadPreviewJob != null )
			loadPreviewJob.cancel();
		loadPreviewJob = new Job("Previewing matched fileset path") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				final IPath[] matched = findPaths(fileset.getFolder(), inc, exc, monitor);
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						if( preview != null && !preview.isDisposed()) {
							preview.setInput(matched);
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadPreviewJob.schedule();
	}
	

	public String getDir() {
		return dir;
	}
	public String getExcludes() {
		return excludes;
	}
	public String getIncludes() {
		return includes;
	}
	public String getName() {
		return name;
	}
	public Fileset getFileset() {
		return fileset;
	}
	
	private static IPath[] findPaths(String dir, String includes, String excludes, IProgressMonitor monitor) {
		return Fileset.findPaths(dir, includes, excludes, monitor);
	}
}