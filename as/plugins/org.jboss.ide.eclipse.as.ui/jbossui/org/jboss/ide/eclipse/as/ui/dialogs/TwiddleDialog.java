package org.jboss.ide.eclipse.as.ui.dialogs;

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

public class TwiddleDialog extends Dialog {

	private static final int EXECUTE_ID = 804;
	private Text query, results;
	private Label queryLabel;
	private ServerProcessModelEntity entity = null;
	private JBossServer server = null;
	private Composite parentComposite;
	private Hyperlink twiddleTutorialLink;
	
	
	public TwiddleDialog(Shell parentShell, IStructuredSelection selection) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		Object o = selection.getFirstElement();
		if( o instanceof ServerProcessModelEntity ) {
			entity = (ServerProcessModelEntity)o;
			String sid = ((ServerProcessModelEntity)o).getServerID();
			IServer iserver = ServerCore.findServer(sid);
			if( iserver != null ) {
				server = JBossServerCore.getServer(iserver);
			}
		}

	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Twiddle Dialog");
		newShell.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE));
	}
	   
	protected Point getInitialSize() {
		return new Point(600, 300);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, EXECUTE_ID, "Execute Twiddle", true);
		createButton(parent, IDialogConstants.OK_ID, "Done",
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
		final JBossServer jbs = server;
		final ServerProcessModelEntity spme = entity;
		getButton(EXECUTE_ID).setEnabled(false);
		Thread t = new Thread() {
			public void run() {
				try {
					TwiddleLauncher launcher = new TwiddleLauncher(3000, 100);
					final ProcessLogEvent launchEvent = launcher.getTwiddleResults(spme, jbs, args);
					ProcessLogEvent[] kids = launchEvent.getChildren();
					String text = "";
					for( int i = 0; i < kids.length; i++ ) {
						text += kids[i].getText() + "\n";
					}
					final String s2 = text;
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
		queryLabel.setText("Twiddle arguments: ");
		FormData queryLabelData = new FormData();
		queryLabelData.left = new FormAttachment(0,5);
		queryLabelData.top = new FormAttachment(0,5);
		queryLabel.setLayoutData(queryLabelData);

		
		twiddleTutorialLink = new Hyperlink(main, SWT.NONE);
		twiddleTutorialLink.setText("Twiddle Tutorial");
		twiddleTutorialLink.setForeground(new Color(null, 0, 0, 255));
		twiddleTutorialLink.setUnderlined(true);
		FormData twiddleTutorialData = new FormData();
		twiddleTutorialData.right = new FormAttachment(100, -5);
		twiddleTutorialData.top = new FormAttachment(0,5);
		twiddleTutorialLink.setLayoutData(twiddleTutorialData);

		
		twiddleTutorialLink.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				String url = "http://docs.jboss.org/jbossas/jboss4guide/r1/html/ch2.chapter.html#d0e4253";
				IWorkbenchBrowserSupport browserSupport = ServerUIPlugin.getInstance().getWorkbench().getBrowserSupport();
				try {
					IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
					browser.openURL(new URL(url));
					close();
				} catch( Exception ee ) {
					
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			} 
			
		} );
		
		
		
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
		
		
		
		return c;
	}

}
