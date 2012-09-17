package org.jboss.ide.eclipse.as.test.publishing.v2;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;

public class Mock2BehaviourDelegate extends LocalJBossBehaviorDelegate {
	private IModulePathFilter filter;
	private boolean useUpstreamBehaviour = false;
	public void setUseSuperclassBehaviour(boolean val) {
		useUpstreamBehaviour = val;
	}
	public IModulePathFilter getPathFilter(IModule[] moduleTree) {
		if( useUpstreamBehaviour )
			return super.getPathFilter(moduleTree);
		return getFilter();
	}
	
	public IModulePathFilter getFilter() {
		return filter;
	}
	public void setFilter(IModulePathFilter filter) {
		this.filter = filter;
	}
	
	public String getBehaviourTypeId() {
		return "mock2";
	}

}
