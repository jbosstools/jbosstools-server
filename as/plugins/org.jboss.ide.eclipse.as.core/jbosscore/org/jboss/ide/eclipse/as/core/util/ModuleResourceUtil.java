package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;

public class ModuleResourceUtil {
	public static int countChanges(IModuleResourceDelta[] deltas) {
		IModuleResource res;
		int count = 0;
		if( deltas == null ) return 0;
		for( int i = 0; i < deltas.length; i++ ) {
			res = deltas[i].getModuleResource();
			if( res != null && res instanceof IModuleFile)
				count++;
			count += countChanges(deltas[i].getAffectedChildren());
		}
		return count;
	}
	
	public static String getParentRelativeURI(IModule[] tree, int index, String defaultName) {
		if( index != 0 ) {
			IEnterpriseApplication parent = (IEnterpriseApplication)tree[index-1].loadAdapter(IEnterpriseApplication.class, null);
			if( parent != null ) {
				String uri = parent.getURI(tree[index]);
				if(uri != null )
					return uri;
			}
			// TODO if we make our own "enterprise app" interface, do that here
		} 
		// return name with extension
		return defaultName;

	}
	

	public static IModuleResource[] getResources(IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Fetching Module Resources", 100); //$NON-NLS-1$
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, 
				ProgressMonitorUtil.submon(monitor, 100));
		if( md == null ) {
			// Deleted Module, TODO handle this differently!
			return new IModuleResource[]{};
		}
		IModuleResource[] members = md.members();
		monitor.done();
		return members;
	}
	
	public static IModuleResource[] getResources(IModule[] tree) throws CoreException {
		return getResources(tree[tree.length-1], new NullProgressMonitor());
	}
	
	public static File getFile(IModuleResource resource) {
		File source = (File)resource.getAdapter(File.class);
		if( source == null ) {
			IFile ifile = (IFile)resource.getAdapter(IFile.class);
			if( ifile != null ) 
				source = ifile.getLocation().toFile();
		}
		return source;
	}

	public static java.io.File getFile(IModuleFile mf) {
		return (IFile)mf.getAdapter(IFile.class) != null ? 
					((IFile)mf.getAdapter(IFile.class)).getLocation().toFile() :
						(java.io.File)mf.getAdapter(java.io.File.class);
	}
	
	
	public static IModule[] combine(IModule[] module, IModule newMod) {
		IModule[] retval = new IModule[module.length + 1];
		for( int i = 0; i < module.length; i++ )
			retval[i]=module[i];
		retval[retval.length-1] = newMod;
		return retval;
	}	
}
