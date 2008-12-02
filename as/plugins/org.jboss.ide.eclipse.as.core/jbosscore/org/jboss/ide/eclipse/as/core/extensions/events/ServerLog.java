package org.jboss.ide.eclipse.as.core.extensions.events;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.adaptor.EclipseLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

public class ServerLog extends EclipseLog {
	public ServerLog(File file) {
		super(file);
	}
	
	public void log(IStatus status) {
		log(getLog(status));
	}
	
	// Hard Copied from PlatformLogWriter. You'd think 
	// they'd make that method static or some shit. 
	protected FrameworkLogEntry getLog(IStatus status) {
		Throwable t = status.getException();
		ArrayList<FrameworkLogEntry> childlist = new ArrayList<FrameworkLogEntry>();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged 
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(getLog(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				childlist.add(getLog(children[i]));
			}
		}

		FrameworkLogEntry[] children = (FrameworkLogEntry[]) (childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]));

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(), status.getMessage(), stackCode, t, children);
	}
}
