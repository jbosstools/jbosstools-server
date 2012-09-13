package org.jboss.ide.eclipse.as.ui.wizards;

import java.util.Arrays;

import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.runtime.DownloadRuntimeToWTPRuntime;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.core.model.IDownloadRuntimeFilter;

public class JBossASDownloadRuntimeFilter implements IDownloadRuntimeFilter {
	private IRuntimeType runtimeType;
	public JBossASDownloadRuntimeFilter(IRuntimeType rtType) {
		this.runtimeType = rtType;
	}
	public boolean accepts(DownloadRuntime runtime) {
		DownloadRuntime[] all = DownloadRuntimeToWTPRuntime.getDownloadRuntimes(runtimeType);
		return Arrays.asList(all).contains(runtime);
	}
	public DownloadRuntime[] filter(DownloadRuntime[] runtimes) {
		return DownloadRuntimeToWTPRuntime.getDownloadRuntimes(runtimeType, runtimes);
	}

}
