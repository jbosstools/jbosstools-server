package org.jboss.ide.eclipse.as.wtp.ui.propertypage;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class NewReferenceWizard extends TaskWizard {

	public static final String COMPONENT = "dependency.component";
	public static final String COMPONENT_PATH = "dependency.component.path";
	public static final String PROJECT = "root.project";
	public static final String ROOT_COMPONENT = "root.component";
	
	public NewReferenceWizard() {
		super("New Reference Wizard", new RootWizardFragment());
		getRootFragment().setTaskModel(getTaskModel());
	}
	
	protected static class RootWizardFragment extends WizardFragment {
		protected void createChildFragments(List<WizardFragment> list) {
			IVirtualComponent component = (IVirtualComponent)getTaskModel().getObject(COMPONENT);
			if( component == null )
				list.add(new NewReferenceRootWizardFragment());
			else {
				WizardFragment[] frags = DependencyPageExtensionManager.getManager().loadAllReferenceWizardFragments();
				for( int i = 0; i < frags.length; i++ ) {
					if( frags[i] instanceof IReferenceEditor ) {
						if( ((IReferenceEditor)frags[i]).canEdit(component)) {
							// accept first one
							list.add(frags[i]);
							return;
						}
					}
				}
			}
		}
	}
	
	public void init(IWorkbench newWorkbench, IStructuredSelection newSelection) {
		// do nothing
	}
}
