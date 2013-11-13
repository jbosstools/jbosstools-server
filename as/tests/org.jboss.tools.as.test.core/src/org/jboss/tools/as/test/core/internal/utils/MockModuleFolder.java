/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.internal.utils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;

public class MockModuleFolder implements IModuleFolder {
	private IModuleResource[] members;
	private IPath relPath; 
	private String name;
	public MockModuleFolder(IPath relPath, String name) {
		this.name = name;
		this.relPath = relPath;
	}
	@Override
	public IPath getModuleRelativePath() {
		return relPath;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModuleResource[] members() {
		return members;
	}
	public void setMembers(IModuleResource[] members) {
		this.members = members;
	}
}
