/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
