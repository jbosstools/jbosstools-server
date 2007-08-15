package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

public class StrippedServerWizardFragment extends WizardFragment {

	private IWizardHandle handle;
	
	private Label deployLabel, nameLabel;
	private Text deployText, nameText;
	private Button browse;
	private String name, deployLoc;

	public StrippedServerWizardFragment() {
	}
	
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
				
		nameLabel = new Label(main, SWT.NONE);
		nameText = new Text(main, SWT.BORDER);
		nameLabel.setText("Server Name");
		
		deployLabel = new Label(main, SWT.NONE);
		deployText = new Text(main, SWT.BORDER);
		browse = new Button(main, SWT.PUSH);
		deployLabel.setText(Messages.deployDirectory);
		browse.setText(Messages.browse);
		
		FormData lData = new FormData();
		lData.top = new FormAttachment(nameText,5);
		lData.left = new FormAttachment(0,5);
		deployLabel.setLayoutData(lData);
		
		FormData tData = new FormData();
		tData.top = new FormAttachment(nameText,5);
		tData.left = new FormAttachment(deployLabel,5);
		tData.right = new FormAttachment(browse, -5);
		deployText.setLayoutData(tData);

		FormData namelData = new FormData();
		namelData.top = new FormAttachment(0,5);
		namelData.left = new FormAttachment(0,5);
		nameLabel.setLayoutData(namelData);
		
		FormData nametData = new FormData();
		nametData.top = new FormAttachment(0,5);
		nametData.left = new FormAttachment(deployLabel,5);
		nametData.right = new FormAttachment(100,-5);
		nameText.setLayoutData(nametData);
		
		FormData bData = new FormData();
		bData.right = new FormAttachment(100,-5);
		bData.top = new FormAttachment(nameText,5);
		browse.setLayoutData(bData);
		
		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		};
		
		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(deployText.getText());
				String x = d.open();
				if( x != null ) 
					deployText.setText(x);
			} 
		});

		deployText.addModifyListener(ml);
		nameText.addModifyListener(ml);
		nameText.setText(getDefaultNameText());
		handle.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.WIZBAN_DEPLOY_ONLY_LOGO));
		return main;
	}

	protected void textChanged() {
		IStatus status = checkErrors();
		if( status.isOK() ) {
			deployLoc = deployText.getText();
			name = nameText.getText();
			handle.setMessage("", IStatus.OK);
			handle.update();
		} else {
			handle.setMessage(status.getMessage(), IStatus.WARNING);
		}
	}
	
	protected IStatus checkErrors() {
		if( findServer(nameText.getText()) != null ) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, "Name in use", null);
		}
		File f = new File(deployText.getText());
		if( !f.exists() || !f.isDirectory() ) {
			return new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, "Folder does not exist", null);
		}
		return new Status(IStatus.OK, JBossServerUIPlugin.PLUGIN_ID, IStatus.OK, "", null);
	}
	
	public void enter() {
		handle.setTitle("Create a new System Copy Server");
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if( s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy)s;
		else
			swc = s.createWorkingCopy();
		
		ServerWorkingCopy swcInternal;
		if( swc instanceof ServerWorkingCopy )  {
			swcInternal = (ServerWorkingCopy)swc;
			deployText.setText(swcInternal.getAttribute(DeployableServer.DEPLOY_DIRECTORY, ""));
		}
	}
	public void exit() {
		textChanged();
		IServer s = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IServerWorkingCopy swc;
		if( s instanceof IServerWorkingCopy)
			swc = (IServerWorkingCopy)s;
		else
			swc = s.createWorkingCopy();
		
		ServerWorkingCopy swcInternal;
		if( swc instanceof ServerWorkingCopy )  {
			swcInternal = (ServerWorkingCopy)swc;
			swcInternal.setName(name);
			swcInternal.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			getTaskModel().putObject(TaskModel.TASK_SERVER, swcInternal);
		}
	}
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		ServerWorkingCopy serverwc2 = (serverWC instanceof ServerWorkingCopy ? ((ServerWorkingCopy)serverWC) : null);
		
		try {
			serverwc2.setServerConfiguration(null);
			serverwc2.setName(name);
			serverwc2.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLoc);
			getTaskModel().putObject(TaskModel.TASK_SERVER, serverwc2);
		} catch( Exception ce ) {
		}
	}

	public boolean isComplete() {
		return checkErrors().isOK();
	}

	public boolean hasComposite() {
		return true;
	}
	
	private String getDefaultNameText() {
		String base = "JBoss deployer";
		if( findServer(base) == null ) return base;
		int i = 1;
		while( ServerCore.findServer(base + " (" + i + ")") != null ) 
			i++;
		return base + " (" + i + ")";
	}
	
	private IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			Server server = (Server) servers[i];
			if (name.equals(server.getName()))
				return server;
		}
		return null;
	}

	
}
