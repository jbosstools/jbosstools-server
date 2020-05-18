/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.wtp.server.launchbar.objects;

import java.util.ArrayList;

public class LaunchedArtifacts {
	public static LaunchedArtifacts instance = new LaunchedArtifacts();
	public static LaunchedArtifacts getDefault() {
		return instance;
	}
	public ArrayList<ModuleArtifactDetailsWrapper> actuallyLaunched = new ArrayList<ModuleArtifactDetailsWrapper>();
	public void markLaunched(ModuleArtifactDetailsWrapper details) {
		if( !actuallyLaunched.contains(details))
			actuallyLaunched.add(details);
	}
	public boolean hasBeenLaunched(ModuleArtifactDetailsWrapper details) { 
		if( actuallyLaunched.contains(details)) {
			return true;
		}
		return false;
	}
	
	public boolean hasBeenLaunched(ModuleArtifactWrapper wrapper) {
		ModuleArtifactDetailsWrapper we = new ModuleArtifactDetailsWrapper(
				wrapper.getName(), wrapper.getArtifactString(), 
				wrapper.getArtifactClass());
		return actuallyLaunched.contains(we);
	}
	
}
