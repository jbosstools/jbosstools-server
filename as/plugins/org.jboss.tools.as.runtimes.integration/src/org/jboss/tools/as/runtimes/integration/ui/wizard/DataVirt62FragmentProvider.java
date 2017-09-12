package org.jboss.tools.as.runtimes.integration.ui.wizard;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBean;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.internal.wizard.FinalizeRuntimeDownloadFragment;
import org.jboss.tools.runtime.ui.wizard.IWorkflowProvider;

public class DataVirt62FragmentProvider implements IWorkflowProvider {

	public DataVirt62FragmentProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canProvideWorkflow(DownloadRuntime dr) {
		if( "jbossdv620runtime".equals(dr.getId())) {
			return true;
		}
		return false;
	}

	@Override
	public WizardFragment[] createFragmentsForRuntime(DownloadRuntime dr) {
		if( "jbossdv620runtime".equals(dr.getId())) {
			return createFragmentsForDV62(dr);
		}
		return new WizardFragment[] {};
	}
	public WizardFragment[] createFragmentsForDV62(DownloadRuntime dr) {
		String title = "Data Virtualization 6.2 Warning";
		String msg = "Data Virtualization 6.2 installer requires EAP 6.4 to be installed prior to installation.";
		String labelMsg = "Data Virtualization 6.2 requires EAP 6.4 to be installed prior to installation.\n\nIt is strongly reccommended you first install EAP 6.4 before proceeding.";
		return createFragmentsForDVxxx(dr, title, msg, labelMsg, JBossServerType.EAP61, "6.4");
	}
	public WizardFragment[] createFragmentsForDVxxx(DownloadRuntime dr, String title, String msg, String label, JBossServerType beanType, String versionPrefix) {
		return new WizardFragment[] {
				new WizardFragment() {
					private IWizardHandle handle;
					private Text underlyingLocation;
					
					@Override
					public boolean hasComposite() {
						return true;
					}

					@Override
					public Composite createComposite(Composite parent, IWizardHandle handle) {
						this.handle = handle;
						getPage().setTitle(title);
						handle.setMessage(msg, IMessageProvider.WARNING);
						return createControl(parent);
					}
					
					protected void validateInstall() {
						File locale = new File(underlyingLocation.getText());
						if( locale.exists() ) {
							ServerBean sb = new ServerBeanLoader(locale).getServerBean();
							if( sb.getBeanType().equals(beanType) && sb.getVersion().equals(versionPrefix)) {
								getTaskModel().putObject(FinalizeRuntimeDownloadFragment.FINALIZE_RUNTIMED_OWNLOAD_FRAGMENT_INSTALLPATH, underlyingLocation.getText());
								setComplete(true);
								handle.setMessage(null, IMessageProvider.NONE);
							} else {
								String msg2 = "The selected folder contains " + sb.getBeanType().getId() + " with version " + sb.getFullVersion();
								handle.setMessage(msg2, IMessageProvider.WARNING);
								setComplete(false);
							}
						} else {
							// file doesnt exist so just reset standard warning
							handle.setMessage(msg, IMessageProvider.WARNING);
							setComplete(false);
						}
						handle.update();
					}
					
					
					public Composite createControl(Composite parent) {
						Composite main = new Composite(parent, SWT.BORDER);
						main.setLayout(new FormLayout());
						Label l = new Label(main, SWT.WRAP);
						l.setText(label);
						l.setLayoutData(FormDataUtility.createFormData2(0,5,null,0,0,5,100,-5));
						
						Label installation = new Label(main, SWT.NONE);
						installation.setText("Installation: ");
						installation.setLayoutData(FormDataUtility.createFormData2(l, 12, null, 0, 0, 5, null, 0));
						
						Button browse = new Button(main, SWT.PUSH);
						browse.setText("Browse...");
						browse.setLayoutData(FormDataUtility.createFormData2(l, 5, null, 0, null, 0, 100, -5));
						
						underlyingLocation = new Text(main, SWT.BORDER | SWT.SINGLE);
						underlyingLocation.setLayoutData(FormDataUtility.createFormData2(l, 5, null, 0, installation, 5, browse, -5));
						
						underlyingLocation.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent e) {
								validateInstall();
							}
						});
						browse.addSelectionListener(new SelectionAdapter() {
							
							@Override
							public void widgetSelected(SelectionEvent e) {
								DirectoryDialog d = new DirectoryDialog(browse.getShell());
								if( underlyingLocation.getText() != null ) 
									d.setFilterPath(underlyingLocation.getText());
								String x = d.open();
								if( x != null ) {
									underlyingLocation.setText(x);
								}
								validateInstall();
							}
						});
						
						setComplete(false);
						handle.update();
						validateInstall();
						return main;
					}
				}
		};
	}

}
