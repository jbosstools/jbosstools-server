package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;

public class PublishUtil {
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


	public static int countMembers(IModule module) {
		try {
			ModuleDelegate delegate = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
			return delegate == null ? 0 : countMembers(delegate.members());
		} catch( CoreException ce ) {}
		return 0;
	}
	public static int countMembers(IModuleResource[] resources) {
		int count = 0;
		if( resources == null ) return 0;
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i] instanceof IModuleFile ) {
				count++;
			} else if( resources[i] instanceof IModuleFolder ) {
				count += countMembers(((IModuleFolder)resources[i]).members());
			}
		}
		return count;
	}

	public static IPath getDeployPath(IModule[] moduleTree, String deployFolder) {
		// TODO This should probably change once 241466 is solved
		IPath root = new Path( deployFolder );
		String type, name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return root.append(new Path(name).lastSegment());
			if( "jst.ear".equals(type)) 
				root = root.append(name + ".ear");
			else if( "jst.web".equals(type)) 
				root = root.append(name + ".war");
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId())) 
				root = root.append("WEB-INF").append("lib").append(name + ".jar");			
			else if( "jst.connector".equals(type)) {
				root = root.append(name + ".rar");
			} else if( "jst.jboss.esb".equals(type)){
				root = root.append(name + ".esb");
			}else
				root = root.append(name + ".jar");
		}
		return root;
	}
	
	public static boolean isBinaryObject(IModule[] moduleTree) {
		String name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return true;
		}
		return false;
	}
	
	public static IModuleResource[] getResources(IModule module) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] members = md.members();
		return members;
	}
	public static IModuleResource[] getResources(IModule[] tree) throws CoreException {
		return getResources(tree[tree.length-1]);
	}
	
	public static java.io.File getFile(IModuleFile mf) {
		return (IFile)mf.getAdapter(IFile.class) != null ? 
					((IFile)mf.getAdapter(IFile.class)).getLocation().toFile() :
						(java.io.File)mf.getAdapter(java.io.File.class);
	}
}
