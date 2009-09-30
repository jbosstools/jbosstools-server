/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.TwiddleLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.server.internal.launch.TwiddleLauncher.ProcessData;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class TwiddleDialog extends TrayDialog {

	private static final int EXECUTE_ID = 2042;
	private Text query, results;
	private Label queryLabel;
	private IServer server = null;
	private Composite parentComposite;
	
	
	public TwiddleDialog(Shell parentShell, Object selection) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		if( selection instanceof IServer ) {
			server = (IServer)selection;
		}

	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.TwiddleDialog);
		newShell.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE));
	}
	   
	protected Point getInitialSize() {
		return new Point(600, 300);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, EXECUTE_ID, Messages.TwiddleDialogExecute, true);
		createButton(parent, IDialogConstants.OK_ID, Messages.TwiddleDialogDone,
				false);
	}
	
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			okPressed();
		} else if (EXECUTE_ID == buttonId) {
			executePressed();
		}
	}
	
	protected void executePressed() {
		final String args = query.getText();
		final Display dis = getShell().getDisplay();
		final IServer jbs = server;
		getButton(EXECUTE_ID).setEnabled(false);
		Thread t = new Thread() {
			public void run() {
				try {
					TwiddleLauncher launcher = new TwiddleLauncher();
					final ProcessData[] datas = launcher.getTwiddleResults(jbs, args, false);
					if( datas.length == 1 ) {
						final String s2 = datas[0].getOut();
						dis.asyncExec(new Runnable() {
							public void run() {
								// reset the default button, focus on the query, and 
								// the entire string selected for easy new queries.
								results.setText(s2);
								getButton(EXECUTE_ID).setEnabled(true);
								Shell shell = parentComposite.getShell();
								query.setFocus();
								query.setSelection(0, query.getText().length());
								if (shell != null) {
									shell.setDefaultButton(getButton(EXECUTE_ID));
								}
							} 
						} );
					}
				} catch( Exception e ) {
					IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.TwiddleDialog_UnexpectedError, e);
					JBossServerUIPlugin.getDefault().getLog().log(status);
				}
				
			}
		};
		t.start();
		
	}
	
	protected Control createDialogArea(Composite parent) {
		this.parentComposite = parent;
		Composite c = (Composite)super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		main.setLayout(new FormLayout());

		queryLabel = new Label(main, SWT.NONE);
		queryLabel.setText(Messages.TwiddleDialogArguments);
		FormData queryLabelData = new FormData();
		queryLabelData.left = new FormAttachment(0,5);
		queryLabelData.top = new FormAttachment(0,5);
		queryLabel.setLayoutData(queryLabelData);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				"org.jboss.ide.eclipse.as.ui.twiddle_usage_tutorial_help"); //$NON-NLS-1$

		query = new Text(main, SWT.BORDER);
		FormData queryData = new FormData();
		queryData.top = new FormAttachment(queryLabel, 5);
		queryData.right = new FormAttachment(100, -5);
		queryData.left = new FormAttachment(0, 5);
		query.setLayoutData(queryData);

		
		
		// Now add stuff to main
		results = new Text(main, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		FormData resultsData = new FormData();
		resultsData.left = new FormAttachment(0,5);
		resultsData.right = new FormAttachment(100,-5);
		resultsData.bottom = new FormAttachment(100,-5);
		resultsData.top = new FormAttachment(query, 5);
		results.setLayoutData(resultsData);
		results.setFont(new Font(null, "Courier New", 8, SWT.NONE)); //$NON-NLS-1$
		
		// set the default text
		try {
			String args = TwiddleLaunchConfiguration.getDefaultArgs(server);
			query.setText(args);
			query.setFocus();
			query.setSelection(args.length());
		} catch( CoreException ce ) {
			// server probably not found. 
			
		}
		return c;
	}

}
