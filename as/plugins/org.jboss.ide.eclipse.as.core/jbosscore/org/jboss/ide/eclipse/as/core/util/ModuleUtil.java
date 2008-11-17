package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;

public class ModuleUtil {
	public static ArrayList<IModule[]> getShallowChildren(IServer server, IModule[] root) {
		ArrayList<IModule[]> list = new ArrayList<IModule[]>();
		IModule[] children = server.getChildModules(root, new NullProgressMonitor());
		// children is { aWar, bWar, cWar } projects
		int length = children == null ? 0 : children.length;
		for( int i = 0; i < length; i++ ) {
			ArrayList<IModule> inner = new ArrayList<IModule>();
			inner.addAll(Arrays.asList(root));
			inner.add(children[i]);
			IModule[] innerMods = inner.toArray(new IModule[inner.size()]);
			list.add(innerMods);
		}
		return list;
	}
	
	public static ArrayList<IModule[]> getDeepChildren(IServer server, IModule[] mod) {
		ArrayList<IModule[]> deep = getShallowChildren(server, mod);
		IModule[] toBeSearched;
		for( int i = 0; i < deep.size(); i++ ) {
			toBeSearched = deep.get(i);
			deep.addAll(getShallowChildren(server, toBeSearched));
		}
		return deep;
	}
	
	public static IModule[] getChildModules(IModule[] module) {
		int last = module.length-1;
		if (module[last] != null && module[last].getModuleType() != null) {
			IModuleType moduleType = module[last].getModuleType();
			if("jst.ear".equals(moduleType.getId())) { //$NON-NLS-1$
				IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module[0]
						.loadAdapter(IEnterpriseApplication.class, null);
				if (enterpriseApplication != null) {
					IModule[] earModules = enterpriseApplication.getModules(); 
					if ( earModules != null) {
						return earModules;
					}
				}
			}
			else if ("jst.web".equals(moduleType.getId())) { //$NON-NLS-1$
				IWebModule webModule = (IWebModule) module[last].loadAdapter(IWebModule.class, null);
				if (webModule != null) {
					IModule[] modules = webModule.getModules();
					return modules;
				}
			}
		}
		return new IModule[0];
	}
	
}
