package org.jboss.ide.eclipse.as.ssh.ui.wizard;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jst.j2ee.model.internal.validation.ValidateBMPBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ssh.Messages;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerDelegate;

public class SCPServerWizardFragment extends WizardFragment {
	private IWizardHandle handle;
	private Text userText, passText, deployText, hostsFileText;
	private ModifyListener listener;
	private SelectionListener browseHostsButtonListener;
	private Button browseHostsFileButton;
	private String user, pass, deploy, hostFile;

	public SCPServerWizardFragment() {
		super();
	}

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		handle.setDescription(Messages.SCPServerDescription);
		handle.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_WIZBAN_NEW_SERVER));

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		addWidgets(main);
		validate();
		return main;
	}
	
	protected void addWidgets(Composite composite) {
		composite.setLayout(new FormLayout());

		Composite inner = new Composite(composite, SWT.NONE);
		inner.setLayout(new GridLayout(3, false));

		FormData innerData = new FormData();
		innerData.top = new FormAttachment(0, 5);
		innerData.left = new FormAttachment(0, 5);
		innerData.right = new FormAttachment(100, -5);
		inner.setLayoutData(innerData);

		GridData textData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		textData.widthHint = 300;
		
		Label label = new Label(inner, SWT.NONE);
		label.setText(Messages.DeployRootFolder);
		deployText = new Text(inner, SWT.BORDER);
		deployText.setText("/home/rob/deployFolder");
		listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValues();
			}
		};
		deployText.addModifyListener(listener);
		deployText.setEnabled(true);
		deployText.setLayoutData(textData);
		
		Label userLabel = new Label(inner, SWT.NONE);
		userLabel.setText(Messages.UserLabel);
		userText = new Text(inner, SWT.BORDER);
		userText.setText("username");
		userText.addModifyListener(listener);
		userText.setEnabled(true);
		userText.setLayoutData(textData);
		
		
		Label passLabel = new Label(inner, SWT.NONE);
		passLabel.setText(Messages.PassLabel);
		passText = new Text(inner, SWT.BORDER);
		passText.setText("password");
		passText.addModifyListener(listener);
		passText.setEnabled(true);
		passText.setLayoutData(textData);
		
		Label hostsLabel = new Label(inner, SWT.NONE);
		hostsLabel.setText(Messages.HostsLabel);
		Composite hostsFileComposite = new Composite(inner, SWT.NONE);
		hostsFileComposite.setLayoutData(textData);
		hostsFileComposite.setLayout(new GridLayout(2,false));
		
		hostsFileText = new Text(hostsFileComposite, SWT.BORDER);
		hostsFileText.setText("/home/username/.ssh/known_hosts");
		hostsFileText.addModifyListener(listener);
		hostsFileText.setEnabled(true);
		GridData hostsFileData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		hostsFileData.widthHint = 200;
		hostsFileData.grabExcessHorizontalSpace = true;
		hostsFileText.setLayoutData(hostsFileData);
		
		browseHostsFileButton = new Button(hostsFileComposite, SWT.PUSH);
		browseHostsFileButton.setText(Messages.browse);
		browseHostsButtonListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browseForHostsSelected();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		browseHostsFileButton.addSelectionListener(browseHostsButtonListener);
	}
	
	protected void browseForHostsSelected() {
		FileDialog d = new FileDialog(new Shell());
		IPath p = ServerUtil.makeGlobal(null, new Path(hostsFileText.getText()));
		d.setFilterPath(p.toString());
		String x = d.open();
		if (x != null) {
			hostsFileText.setText(x);
		}
	}

	protected void updateValues() {
		user = userText.getText();
		pass = passText.getText();
		deploy = deployText.getText();
		hostFile = hostsFileText.getText();
		validate();
	}
	
	public void enter() {
	}
	public void exit() {
	}
	public boolean hasComposite() {
		return true;
	}
	
	protected void validate() {
		if( hostFile == null || !(new File(hostFile).exists()) || !(new File(hostFile).isFile()))
			handle.setMessage("Host file must exist", IMessageProvider.ERROR);
		else
			handle.setMessage(null, IMessageProvider.NONE);
		handle.update();
	}
	
	public boolean isComplete() {
		return handle.getMessageType() == IMessageProvider.NONE;
	}
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		SSHServerDelegate server = (SSHServerDelegate)serverWC.loadAdapter(SSHServerDelegate.class, new NullProgressMonitor());
		server.setUsername(user); //$NON-NLS-1$
		server.setPassword(pass); //$NON-NLS-1$
		server.setHostsFile(hostFile);
		server.setDeployFolder(deploy);
	}
}
