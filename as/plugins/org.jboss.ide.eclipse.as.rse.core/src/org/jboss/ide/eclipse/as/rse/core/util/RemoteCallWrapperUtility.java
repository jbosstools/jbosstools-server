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
package org.jboss.ide.eclipse.as.rse.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.rse.core.RSECorePlugin;
import org.jboss.ide.eclipse.as.rse.core.RSEFrameworkUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.tools.foundation.core.jobs.BarrierProgressWaitJob;
import org.jboss.tools.foundation.core.jobs.BarrierProgressWaitJob.IRunnableWithProgress;

/**
 * A utility class to wrap remote calls properly.
 * Makes use of foundation's barrier wait jobs.
 * 
 * @since 3.0
 */
public class RemoteCallWrapperUtility {
	
	/*
	 * An extension to IRunnableWithProgress to add a name
	 */
	public abstract static class NamedRunnableWithProgress implements IRunnableWithProgress {
		private String name;
		public NamedRunnableWithProgress(String name){
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	
	private static IStatus generateFailStatus(IServer server, String message, String resource, Exception sme) {
		String exceptionMsg = sme.getMessage();
		if( "Missing element for : ''".equals(exceptionMsg)) {
			sme = new Exception("The requested path is not found on the remote system.", sme);
		}
		String connectionName = RSEUtils.getRSEConnectionName(server);
		IHost host = connectionName == null ? null : RSEFrameworkUtils.findHost(connectionName);
		IStatus s = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
				NLS.bind(message, resource, host == null ? null : host.getName()), sme);
		return s;
	}

	
	public static Exception wrapRemoteCallStatusTimeLimit(IServer server, final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			final int maxDelay, final IProgressMonitor monitor) {
		Thread t = new Thread("Remote call timer") {
			public void run() {
				try {
					Thread.sleep(maxDelay);
					monitor.setCanceled(true);
				} catch( InterruptedException ie) {
					// Do Nothing
				}
			}
		};
		t.start();
		try {
			wrapRemoteCall(server, runnable, remoteResource, failErrorMessage, true, monitor);
		} catch (CoreException e) {
			if( e.getStatus().getSeverity() == IStatus.CANCEL ) {
				return new CoreException(new Status(IStatus.CANCEL,RSECorePlugin.PLUGIN_ID, 
						"The remote operation has been canceled because it did not finish in the alloted time (" + maxDelay + "ms)"));
			}
			return e;
		} catch (RuntimeException e) {
			return e;
		} finally {
			t.interrupt();
		}
		return null;
	}
	
	public static IStatus wrapRemoteCall(IServer server, final NamedRunnableWithProgress runnable, 
			final String remoteResource, final String failErrorMessage, 
			final boolean alwaysThrow, final IProgressMonitor monitor) throws CoreException, RuntimeException  {
		monitor.setTaskName(runnable.getName());
		BarrierProgressWaitJob j = new BarrierProgressWaitJob(runnable.getName(),  runnable);
		j.schedule();
		// This join will also poll the provided monitor for cancelations
		j.monitorSafeJoin(monitor);
		if( j.getReturnValue() != null) {
			IStatus s = (IStatus)j.getReturnValue();
			return s;
		}
		if( j.getThrowable() != null ) {
			if(j.getThrowable() instanceof SystemMessageException) {
				IStatus stat = generateFailStatus(server, failErrorMessage, 
						remoteResource, ((SystemMessageException)j.getThrowable()));
				if( alwaysThrow )
					throw new CoreException(stat);
				else {
					return stat;
				}
			}
			if( j.getThrowable() instanceof CoreException )
				throw new CoreException(((CoreException)j.getThrowable()).getStatus());
			throw new RuntimeException(j.getThrowable());
		}
		return Status.CANCEL_STATUS;
	}

}
