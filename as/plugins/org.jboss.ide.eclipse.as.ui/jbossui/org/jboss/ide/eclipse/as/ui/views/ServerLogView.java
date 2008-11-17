package org.jboss.ide.eclipse.as.ui.views;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.views.log.LogView;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.IServerLogListener;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;

public class ServerLogView extends LogView implements IServerLogListener {
	public static final String VIEW_ID = "org.jboss.ide.eclipse.as.ui.view.serverLogView";

	private IServer server;
	private boolean subclassLogging = false;
	public ServerLogView() {
		super();
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		setLogFile(ServerLogger.getDefault().getServerLogFile(null));
	}

	public void setServer(IServer server) {
		if( this.server != null )
			ServerLogger.getDefault().removeListener(this.server, this);
		this.server = server;
		ServerLogger.getDefault().addListener(server, this);
		setLogFile(ServerLogger.getDefault().getServerLogFile(server));
	}
	
	public void dispose() {
		setLogFile(null);
		super.dispose();
	}
	
	protected void setLogFile(File f) {
		super.setLogFile(f);
	}

	// This must be done because too many fields / classes are private
	// and the super.logging(IStatus, String) method does nothing
	// if the platform log is not opened. Which is stupid. 
	public void logging(IStatus status, IServer server) {
		if( server != null && server.equals(this.server)) {
			subclassLogging = true;
			super.logging(status, null);
			subclassLogging = false;
		}
	}
	
	public boolean isPlatformLogOpen() {
		return subclassLogging ? true : super.isPlatformLogOpen();
	}
}
