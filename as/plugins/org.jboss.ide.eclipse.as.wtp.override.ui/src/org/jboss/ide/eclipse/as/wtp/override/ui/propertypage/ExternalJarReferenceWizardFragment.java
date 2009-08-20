package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

public class ExternalJarReferenceWizardFragment extends JarReferenceWizardFragment {
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		Composite c = super.createComposite(parent, handle);
		handle.setTitle("Add an External Jar Reference");
		handle.setDescription("Here you can reference a filesystem Jar\n"
						+ "This is not a suggested use-case, but is here for backwards compatability.");
		return c;
	}

	protected void buttonPressed() {
		selected = BuildPathDialogAccess
			.chooseExternalJAREntries(browse.getShell());
		viewer.refresh();
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IVirtualComponent rootComponent = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
		if (selected != null && selected.length > 0) {
			ArrayList<IVirtualComponent> compList = new ArrayList<IVirtualComponent>();
			ArrayList<String> paths = new ArrayList<String>();
			for (int i = 0; i < selected.length; i++) {
				// IPath fullPath = project.getFile(selected[i]).getFullPath();
				String type = VirtualArchiveComponent.LIBARCHIVETYPE
						+ IPath.SEPARATOR;
				IVirtualComponent archive = ComponentCore
						.createArchiveComponent(rootComponent.getProject(),
								type + selected[i].toString());
				compList.add(archive);
				paths.add(selected[i].lastSegment());
			}
			IVirtualComponent[] components = (IVirtualComponent[]) compList.toArray(new IVirtualComponent[compList.size()]);
			String[] paths2 = (String[]) paths.toArray(new String[paths.size()]);
			getTaskModel().putObject(NewReferenceWizard.COMPONENT, components);
			getTaskModel().putObject(NewReferenceWizard.COMPONENT_PATH, paths2);
		}
	}
}
