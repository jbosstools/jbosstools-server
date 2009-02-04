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
package org.jboss.ide.eclipse.archives.webtools.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.ui.util.composites.FilesetPreviewComposite;
import org.jboss.ide.eclipse.archives.webtools.Messages;

public class FilesetDialog extends TitleAreaDialog {
	protected Fileset fileset;
	private String name, dir, includes, excludes;
	private Button browse;
	private Text includesText, excludesText, folderText, nameText;
	private Composite main;
	private FilesetPreviewComposite preview;
	protected FilesetDialog(Shell parentShell, String defaultLocation) {
		super(parentShell);
		this.fileset = new Fileset();
		this.fileset.setFolder(defaultLocation);

	}
	protected FilesetDialog(Shell parentShell, Fileset fileset) {
		super(parentShell);
		this.fileset = (Fileset)fileset.clone();
	}
	protected Point getInitialSize() {
		//return new Point(400, 150);
		Point p = super.getInitialSize();
		return new Point(500, p.y);
	}
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.FilesetsNewFileset);
	}

	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.FilesetsDialogTitle);
		setMessage(Messages.FilesetsDialogMessage);

		Composite sup = (Composite) super.createDialogArea(parent);
		main = new Composite(sup, SWT.NONE);
		main.setLayout(new GridLayout(3, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		fillArea(main);

		nameText.setText(fileset.getName());
		folderText.setText(fileset.getFolder());
		includesText.setText(fileset.getIncludesPattern());
		excludesText.setText(fileset.getExcludesPattern());

		addListeners();
		return sup;
	}

	protected void addListeners() {
		ModifyListener mListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textModified();
			}
		};
		nameText.addModifyListener(mListener);
		folderText.addModifyListener(mListener);
		includesText.addModifyListener(mListener);
		excludesText.addModifyListener(mListener);

		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(folderText.getText());
				String x = d.open();
				if( x != null )
					folderText.setText(x);
			}
		});
	}

	protected void textModified() {
		name = nameText.getText();
		dir = folderText.getText();
		includes = includesText.getText();
		excludes = excludesText.getText();
		fileset.setName(name);
		fileset.setFolder(dir);
		fileset.setIncludesPattern(includes);
		fileset.setExcludesPattern(excludes);
		updatePreview();
	}
	protected void fillArea(Composite main) {
		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText(Messages.FilesetsNewName);

		nameText = new Text(main, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

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

	private void updatePreview() {
		preview.setInput(findPaths(dir, includes, excludes));
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
	
	
	private static IPath[] findPaths(String dir, String includes, String excludes) {
		IPath[] paths = new IPath[0];
		try {
			if( dir != null ) {
				DirectoryScanner scanner  =
					DirectoryScannerFactory.createDirectoryScanner(dir, null, includes, excludes, null, false, 1, true);
				if( scanner != null ) {
					String[] files = scanner.getIncludedFiles();
					paths = new IPath[files.length];
					for( int i = 0; i < files.length; i++ ) {
						paths[i] = new Path(files[i]);
					}
				}
			}
		} catch( IllegalStateException ise ) {}
		return paths;
	}

}