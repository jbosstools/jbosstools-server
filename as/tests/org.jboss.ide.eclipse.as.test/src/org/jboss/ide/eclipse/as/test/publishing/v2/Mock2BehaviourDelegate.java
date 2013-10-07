/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.test.publishing.v2;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;

public class Mock2BehaviourDelegate extends LocalJBossBehaviorDelegate {
	private IModulePathFilter filter;
	private boolean useDefaultBehav = false;
	public void setUseSuperclassBehaviour(boolean val) {
		useDefaultBehav = val;
	}
	public IModulePathFilter getPathFilter(IModule[] moduleTree) {
		if( useDefaultBehav )
			return ResourceModuleResourceUtil.findDefaultModuleFilter(moduleTree[moduleTree.length-1]);
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
