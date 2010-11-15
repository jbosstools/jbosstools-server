/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect() 
 * Martin Oberhuber (Wind River) - [216266] Consider stateless subsystems (supportsSubSystemConnect==false)
 * David McKnight   (IBM)        - [237970]  Subsystem.connect( ) fails for substituting host name when isOffline( ) is true
 *******************************************************************************/

package org.jboss.ide.eclipse.as.rse.core.xpl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * This is the action for connecting all subsystems for a given connection.
 * For some reason it was hidden in a ui package... 
 */
public class ConnectAllSubsystemsUtil {
	private IHost _connection;
	public ConnectAllSubsystemsUtil(IHost host) {
		this._connection = host;
	}
		
	public IStatus run(IProgressMonitor monitor)
	{
	    List<IConnectorService> failedSystems = new ArrayList<IConnectorService>();
		try 
		{
		    //forced instantiation of all subsystems
		    ISubSystem[] subsystems = _connection.getSubSystems();
		    for (int i = 0; i < subsystems.length; i++)
		    {
		        ISubSystem subsystem = subsystems[i];
		        IConnectorService system = subsystem.getConnectorService();
		        if (!subsystem.isConnected()
		          && subsystem.getSubSystemConfiguration().supportsSubSystemConnect()
		          && !failedSystems.contains(system)) {
		            try {
		                subsystem.connect(monitor, false);
		            } catch (SystemMessageException e) {
						//TODO should we collect all messages and just show one dialog with a MultiStatus?
		                failedSystems.add(system);
						//SystemMessageDialog.displayMessage(e);
					} catch (Exception e) {
		                failedSystems.add(system);
		                if ((e instanceof InterruptedException) || (e instanceof OperationCanceledException)) {
			                // if the user was prompted for password and cancelled
			                // or if the connect was interrupted for some other reason
			                // we don't attempt to connect the other subsystems
							break;
		                }
//						SystemBasePlugin.logError(
//								e.getLocalizedMessage()!=null ? e.getLocalizedMessage() : e.getClass().getName(),
//								e);
					}
		        }
		    }
		} 
		catch (Exception exc) 
		{
		} 	// msg already shown	
		if (failedSystems.size() > 0)
		{
			return Status.CANCEL_STATUS;
		}
		else
		{
			return Status.OK_STATUS;
		}
	}	
}
