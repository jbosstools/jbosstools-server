package org.jboss.ide.eclipse.as.wtp.override.core.vcf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;

public class JBTVirtualFolder extends VirtualFolder {
	private JBTVirtualComponent component;
	public JBTVirtualFolder(IProject aComponentProject, 
			IPath aRuntimePath, JBTVirtualComponent component) {
		super(aComponentProject, aRuntimePath);
		this.component = component;
	}

	public IVirtualResource[] superMembers() throws CoreException {
		return superMembers(IResource.NONE);
	}
	
	public IVirtualResource[] superMembers(int memberFlags) throws CoreException {
		return super.members(memberFlags);
	}
	
	public boolean isDynamicComponent(IVirtualFile vFile){
		return component.isDynamicComponent(vFile);
	}
	
	/**
	 * For now, just rip out files with .jar, .rar, or .war file extensions, because these are
	 * the only files automatically added dyamically
	 */
	public IVirtualResource[] members(int memberFlags) throws CoreException {
		// If this component doesn't expose loose refs, just give normal answer
		if( !component.exposesLooseReferences() ) {
			return superMembers(memberFlags);
		}
		
		IVirtualResource[] members = superMembers(memberFlags);
		List virtualResources = new ArrayList();
		boolean shouldAdd = true;
		for (int i = 0; i < members.length; i++) {
			shouldAdd = true;
			if (IVirtualResource.FILE == members[i].getType()) {
				if(isDynamicComponent((IVirtualFile)members[i])){
					shouldAdd = false;
				}
			}
			if (shouldAdd) {
				virtualResources.add(members[i]);
			}
		}
		return (IVirtualResource[]) virtualResources
				.toArray(new IVirtualResource[virtualResources.size()]);
	}
}
