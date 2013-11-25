package org.jboss.tools.as.test.core.internal.utils.classpath;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class WorkspaceTestUtil {
	
	public static void setAutoBuildEnabled( boolean b) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (workspace.isAutoBuilding()) {
                IWorkspaceDescription description = workspace.getDescription();
                description.setAutoBuilding(false);
                try {
                	workspace.setDescription(description);
                }catch(CoreException ce) {}
        }
	}
	
	public static boolean isAutoBuildEnabled() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription description = workspace.getDescription();
        return description.isAutoBuilding();
	}

}
