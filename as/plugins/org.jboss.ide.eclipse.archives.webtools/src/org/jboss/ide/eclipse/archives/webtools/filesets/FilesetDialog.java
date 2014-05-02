/*******************************************************************************
 * Copyright (c) 2007-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.filesets;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetComposite.IFilesetCompositeErrorDisplay;

public class FilesetDialog extends TitleAreaDialog implements IFilesetCompositeErrorDisplay {
	protected Fileset fileset;
	private FilesetComposite main;
	private boolean showViewer = true;

	protected FilesetDialog(Shell parentShell, String defaultLocation, IServer server) {
		super(parentShell);
		this.fileset = new Fileset();
		this.fileset.setFolder(defaultLocation);
		this.fileset.setServer(server);
		this.fileset.setIncludesPattern(getDefaultIncludesPattern());
	}
	protected FilesetDialog(Shell parentShell, Fileset fileset) {
		super(parentShell);
		this.fileset = (Fileset)fileset.clone();
	}
	
	protected String getDefaultIncludesPattern() {
		return "**/*.xml"; //$NON-NLS-1$
	}
	
	public void setShowViewer(boolean val) {
		showViewer = val;
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
		main = new FilesetComposite(sup, fileset, showViewer);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setErrorDisplay(this);
		getShell().layout();
		updateError(main.getValidationError());
		return sup;
	}
	
	public void updateError(String msg) {
		setErrorMessage(msg);
		if(getButton(IDialogConstants.OK_ID) != null)
			getButton(IDialogConstants.OK_ID).setEnabled(msg == null);
	}
	
	public void create() {
		super.create();
		updateError(main.getValidationError());
	}
	
	public String getDir() {
		return main.getDir();
	}
	public String getExcludes() {
		return main.getExcludes();
	}
	public String getIncludes() {
		return main.getIncludes();
	}
	public String getName() {
		return main.getName();
	}
	public Fileset getFileset() {
		return main.getFileset();
	}
}