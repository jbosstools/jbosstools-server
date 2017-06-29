package org.jboss.tools.as.runtimes.integration.ui.wizard;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
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
		return new WizardFragment[] {
				new WizardFragment() {

					@Override
					public boolean hasComposite() {
						return true;
					}

					@Override
					public Composite createComposite(Composite parent, IWizardHandle handle) {
						getPage().setTitle("Data Virtualization 6.2 Warning");
						handle.setMessage("Data Virtualization 6.2 installer requires EAP 6.4 to be installed prior to installation.", IMessageProvider.WARNING);
						return createControl(parent);
					}
					
					public Composite createControl(Composite parent) {
						Composite main = new Composite(parent, SWT.NONE);
						main.setLayout(new FillLayout());
						Label l = new Label(main, SWT.WRAP);
						l.setText("Data Virtualization 6.2 requires EAP 6.4 to be installed prior to installation.\n\nIt is strongly reccommended you first install EAP 6.4 before proceeding.");
						return main;
					}
				}
		};
	}

}
