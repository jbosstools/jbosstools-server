package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

public class VariableReferenceWizardFragment extends JarReferenceWizardFragment {
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		Composite c = super.createComposite(parent, handle);
		handle.setTitle("Add a Variable Reference");
		handle.setDescription("Here you can reference a variable which maps to a single jar.\n"
						+ "This is not a suggested use-case, but is here for backwards compatability.");
		return c;
	}

	protected void buttonPressed() {
		selected = BuildPathDialogAccess.chooseVariableEntries(
				browse.getShell(), new Path[0]);
		viewer.refresh();
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IVirtualComponent rootComponent = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
		if (selected != null && selected.length > 0) {
			ArrayList<IVirtualComponent> compList = new ArrayList<IVirtualComponent>();
			ArrayList<String> paths = new ArrayList<String>();
			for (int i = 0; i < selected.length; i++) {
				IPath resolvedPath = JavaCore.getResolvedVariablePath(selected[i]);
				java.io.File file = new java.io.File(resolvedPath.toOSString());
				if (file.isFile() && file.exists()) {
					String type = VirtualArchiveComponent.VARARCHIVETYPE
							+ IPath.SEPARATOR;
					IVirtualComponent archive = ComponentCore
							.createArchiveComponent(rootComponent.getProject(),
									type + selected[i].toString());
					compList.add(archive);
					paths.add(resolvedPath.lastSegment());
				}
			}
			IVirtualComponent[] components = (IVirtualComponent[]) compList.toArray(new IVirtualComponent[compList.size()]);
			String[] paths2 = (String[]) paths.toArray(new String[paths.size()]);
			getTaskModel().putObject(NewReferenceWizard.COMPONENT, components);
			getTaskModel().putObject(NewReferenceWizard.COMPONENT_PATH, paths2);
		}
	}
}
