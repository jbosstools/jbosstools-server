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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.model.IModuleFile;

public class MockModuleFile implements IModuleFile {
	private IPath relPath; 
	private String name;
	private File file;
	public MockModuleFile(IPath relPath, String name) {
		this.name = name;
		this.relPath = relPath;
	}
	@Override
	public IPath getModuleRelativePath() {
		return relPath;
	}
	
	public void setFile(File f) {
		this.file = f;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if( adapter == File.class)
			return file;
		return null;
	}
	@Override
	public long getModificationStamp() {
		return file == null ? 0 : file.exists() ?file.lastModified() : 0;
	}
}
