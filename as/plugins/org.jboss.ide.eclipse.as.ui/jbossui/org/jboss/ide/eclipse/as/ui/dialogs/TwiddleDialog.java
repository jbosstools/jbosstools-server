package org.jboss.ide.eclipse.as.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
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
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher.ProcessData;
import org.jboss.ide.eclipse.as.ui.Messages;

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
		//newShell.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE));
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
					e.printStackTrace();
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

		Workbench.getInstance().getHelpSystem().setHelp(getShell(),
				"org.jboss.ide.eclipse.as.ui.twiddle_usage_tutorial_help");

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
		results.setFont(new Font(null, "Courier New", 8, SWT.NONE));
		
		
		
		// set the default text
		try {
			ILaunchConfiguration config = JBossServerLaunchConfiguration.setupLaunchConfiguration(server, JBossServerLaunchConfiguration.TWIDDLE);
			String args = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			query.setText(args);
			query.setFocus();
			query.setSelection(args.length());
		} catch( CoreException ce ) {
		}
		return c;
	}

}
