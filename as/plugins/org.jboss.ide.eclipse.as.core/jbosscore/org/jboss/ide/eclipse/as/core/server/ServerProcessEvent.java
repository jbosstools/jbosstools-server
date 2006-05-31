package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;

public class ServerProcessEvent {
	
	private String eventType;
	private String processType;
	private ProcessData[] processes;
	private ILaunchConfiguration config;
	
	public ServerProcessEvent(String eventType, String processType, ProcessData[] processes, ILaunchConfiguration config) {
		this.eventType = eventType;
		this.processType = processType;
		this.processes = processes;
		this.config = config;
	}
	
	public String getEventType() {
		return eventType;
	}
	public String getProcessType() {
		return processType;
	}
	public ProcessData[] getProcessDatas() {
		return processes;
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}
}
