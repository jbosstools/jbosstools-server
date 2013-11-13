/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.net.URI;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

public class JMXServerDeploymentScannerAdditions extends AbstractDeploymentScannerAdditions {
	public JMXServerDeploymentScannerAdditions() {
		
	}
	public boolean accepts(IServer server) {
		if( !LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(getServerMode(server)))
			return false;
		
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		boolean hasJMXProvider = jbs != null && jbs.hasJMXProvider();
		boolean jmxDeploymentScanner = props != null && props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_JMX_SUPPORT;
		if(hasJMXProvider && jmxDeploymentScanner) {
			return true;
		}
		return false;
	}

	protected void ensureScannersAdded(final IServer server, final String[] folders) {
		ExtensionManager.getDefault().getJMXRunner().beginTransaction(server, this);
		IServerJMXRunnable r = new IServerJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ensureDeployLocationAdded(server, connection, folders);
			}
		};
		try {
			ExtensionManager.getDefault().getJMXRunner().run(server, r);
		} catch( CoreException jmxe ) {
			IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
					IEventCodes.ADD_DEPLOYMENT_FOLDER_FAIL, 
					Messages.AddingJMXDeploymentFailed, jmxe);
			ServerLogger.getDefault().log(server, status);
		}
	}
	
	private void ensureDeployLocationAdded(IServer server, 
			MBeanServerConnection connection, String[] folders2) throws Exception {
		for( int i = 0; i < folders2.length; i++ ) {
			String asURL = encode(folders2[i]);
			Trace.trace(Trace.STRING_FINER, "Adding Deployment Scanner: " + asURL); //$NON-NLS-1$
			ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
			Object o = connection.invoke(name, IJBossRuntimeConstants.addURL, new Object[] { asURL }, new String[] {String.class.getName()});
			System.out.println(o);
		}
	}

	private String encode(String folder) throws Exception {
		folder = folder.replace("\\", "/");  //$NON-NLS-1$//$NON-NLS-2$
		if (! folder.startsWith("/")) { //$NON-NLS-1$
			folder = "/" + folder; //$NON-NLS-1$
		}
		URI uri = new URI("file", null, folder, null); //$NON-NLS-1$
		//return URLEncoder.encode(uri.toASCIIString());
		return uri.toASCIIString();
	}
	public void removeAddedDeploymentScanners(IServer server) {
		// Unsupported
	}
	public Job getRemoveDeploymentScannerJob(IServer server) {
		// Unsupported
		return null;
	}
	
	protected void suspendDeployment(IServer server, MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
		launchDeployCommand(server, connection, name, IJBossRuntimeConstants.STOP, monitor);
	}
	

	
	protected void resumeDeployment(IServer server, MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Resuming Deployment Scanner", 1000); //$NON-NLS-1$
		ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
		launchDeployCommand(server, connection, name, IJBossRuntimeConstants.START, monitor);
		monitor.worked(1000);
		monitor.done();
	}
	
	protected void launchDeployCommand(final IServer server, final MBeanServerConnection connection, final ObjectName objectName, 
			final String methodName, IProgressMonitor monitor) throws Exception {
		final Exception[] e = new Exception[1];
		final Object waitObject = new Object();
		final Boolean[] subtaskComplete = new Boolean[1];
		subtaskComplete[0] = new Boolean(false);
		Thread t = new Thread() {
			public void run() {
				Exception exception = null;
				try {
					executeDeploymentCommand(connection, objectName, methodName);
				} catch( Exception ex ) {
					exception = ex;
				}
				synchronized(waitObject) {
					e[0] = exception;
					subtaskComplete[0] = new Boolean(true);
					waitObject.notifyAll();
				}
			}
		};
		t.start();
		int count = 0;
		while(t.isAlive() && !monitor.isCanceled() && count <= 4000) {
			count+= 1000;
			synchronized(waitObject) {
				if( subtaskComplete[0].booleanValue() )
					break;
				waitObject.wait(1000);
			}
		}
		synchronized(waitObject) {
			if( !subtaskComplete[0].booleanValue()) {
				t.interrupt();
				IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.DEPLOYMENT_SCANNER_TRANSITION_CANCELED, Messages.JMXScannerCanceled, null);
				ServerLogger.getDefault().log(server, status);
			} else if( e[0] != null ) {
				String error = methodName.equals(IJBossRuntimeConstants.START) ? Messages.JMXResumeScannerError : Messages.JMXPauseScannerError;
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.DEPLOYMENT_SCANNER_TRANSITION_FAILED, error, e[0]);
				ServerLogger.getDefault().log(server, status);
			}
		}
	}
	

	protected void executeDeploymentCommand(MBeanServerConnection connection, ObjectName objectName, String methodName) throws Exception {
		connection.invoke(objectName, methodName, new Object[] {  }, new String[] {});
	}
	
	public Job getSuspendScannerJob(final IServer server) {
		return new Job("Suspend Deployment Scanner") { //$NON-NLS-1$
			protected IStatus run(final IProgressMonitor monitor) {
				ExtensionManager.getDefault().getJMXRunner().beginTransaction(server, this);
				IServerJMXRunnable r = new IServerJMXRunnable() {
					public void run(MBeanServerConnection connection) throws Exception {
						suspendDeployment(server, connection, monitor);
					}
				};
				try {
					ExtensionManager.getDefault().getJMXRunner().run(server, r);
				} catch( CoreException jmxe ) {
					IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.SUSPEND_DEPLOYMENT_SCANNER, Messages.JMXPauseScannerError, jmxe);
					ServerLogger.getDefault().log(server, status);
					return status;
				}
				return Status.OK_STATUS;
			}
		};
	}

	public Job getResumeScannerJob(final IServer server) {
		return new Job("Suspend Deployment Scanner") { //$NON-NLS-1$
			protected IStatus run(final IProgressMonitor monitor) {
				IServerJMXRunnable r = new IServerJMXRunnable() {
					public void run(MBeanServerConnection connection) throws Exception {
						resumeDeployment(server, connection, monitor);
					}
				};
				try {
					ExtensionManager.getDefault().getJMXRunner().run(server, r);
				} catch( CoreException jmxe ) {
					IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.RESUME_DEPLOYMENT_SCANNER, 
							Messages.JMXResumeScannerError, jmxe);
					ServerLogger.getDefault().log(server, status);
					return status;
				} finally {
					ExtensionManager.getDefault().getJMXRunner().endTransaction(server, this);
				}
				return Status.OK_STATUS;
			}
		};
	}

	@Override
	public void suspendScanners(IServer server) {
		Job j = getSuspendScannerJob(server);
		j.schedule();
		try {
			j.join();
		} catch(InterruptedException ie) {
			// Ignore
		}
	}
	
	@Override
	public void resumeScanners(IServer server) {
		Job j = getResumeScannerJob(server);
		j.schedule();
		try {
			j.join();
		} catch(InterruptedException ie) {
			// Ignore
		}
	}
}
