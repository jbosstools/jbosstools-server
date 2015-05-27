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
