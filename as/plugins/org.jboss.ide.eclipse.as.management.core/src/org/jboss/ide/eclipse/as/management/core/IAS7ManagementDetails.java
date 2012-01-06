package org.jboss.ide.eclipse.as.management.core;

import org.eclipse.wst.server.core.IServer;

public interface IAS7ManagementDetails {
	public String getHost();
	public int getManagementPort();	
	public String getManagementUsername();
	public String getManagementPassword();	
	public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException;	
	public IServer getServer();
}
