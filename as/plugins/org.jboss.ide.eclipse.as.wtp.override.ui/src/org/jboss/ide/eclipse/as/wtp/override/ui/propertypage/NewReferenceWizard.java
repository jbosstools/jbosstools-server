package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class NewReferenceWizard extends TaskWizard {

	public static final String COMPONENT = "dependency.component";
	public static final String COMPONENT_PATH = "dependency.component.path";
	public static final String PROJECT = "root.project";
	public static final String ROOT_COMPONENT = "root.component";
	
	public NewReferenceWizard() {
		super("New Reference Wizard", new WizardFragment() {
			protected void createChildFragments(List<WizardFragment> list) {
				list.add(new NewReferenceRootWizardFragment());
			}
		});
	}

	public void init(IWorkbench newWorkbench, IStructuredSelection newSelection) {
		// do nothing
	}
}
