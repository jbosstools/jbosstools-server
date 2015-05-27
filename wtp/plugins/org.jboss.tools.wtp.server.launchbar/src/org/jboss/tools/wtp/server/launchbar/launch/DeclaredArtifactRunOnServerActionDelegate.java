/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar.launch;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.IClient;
import org.eclipse.wst.server.core.internal.ILaunchableAdapter;
import org.eclipse.wst.server.core.model.ModuleArtifactDelegate;
import org.eclipse.wst.server.ui.internal.EclipseUtil;
import org.eclipse.wst.server.ui.internal.LaunchClientJob;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.actions.RunOnServerActionDelegate;
import org.eclipse.wst.server.ui.internal.viewers.ModuleArtifactComposite;
import org.eclipse.wst.server.ui.internal.wizard.RunOnServerWizard;


/**
 * A subclass of wst.servertools' RunOnServerActionDelegate
 * which is instantiated with a given server and artifact, rather than going
 * through the workflow to choose one. 
 * 
 * Due to the structure of RunOnServerActionDelegate,
 * this class must copy a fairly large amount of logic to override
 * the module artifact.
 */
public class DeclaredArtifactRunOnServerActionDelegate extends DeclaredServerRunOnServerActionDelegate {
	private IModuleArtifact[] artifact;
	public DeclaredArtifactRunOnServerActionDelegate(IServer s, IModuleArtifact[] artifact) {
		super(s);
		this.artifact = artifact;
	}

	
	/**
	 * 
	 * We have to subclass and override this gigantic method. Ugh. 
	 * 
	 * Run the resource on a server.
	 */
	protected void run() {
		final IModuleArtifact[] moduleArtifacts = artifact; //ServerPlugin.getModuleArtifacts(selection);
		
		if (moduleArtifacts == null || moduleArtifacts.length == 0 || moduleArtifacts[0] == null) {
			EclipseUtil.openError(Messages.errorNoArtifact);
			if (Trace.FINEST) {
				Trace.trace(Trace.STRING_FINEST, "No module artifact found");
			}
			return;
		}
		
		Shell shell2 = null;
		if (window != null)
			shell2 = window.getShell();
		else {
			try {
				shell2 = ServerUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
			} catch (Exception e) {
				// ignore
			}
			if (shell2 == null)
				shell2 = Display.getDefault().getActiveShell();
		}
		final Shell shell = shell2;
		final IAdaptable info = new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (Shell.class.equals(adapter))
					return shell;
				if (String.class.equals(adapter))
					return "user";
 				return null;
			}
		};
		
		// If there is more than 1 moduleArtifact, get a valid ModuleArtifact that we can use for launching
		// TODO The ModuleArtifactComposite should be part of the RunOnServerWizard
		final IModuleArtifact moduleArtifact;
		if (moduleArtifacts.length > 1) {
			ModuleArtifactComposite artifactComposite = new ModuleArtifactComposite(shell, moduleArtifacts, launchMode);
			if (artifactComposite.open() == Window.CANCEL)
				return;
			
			moduleArtifact = artifactComposite.getSelection();
		} else
			moduleArtifact = moduleArtifacts[0];
		
		if (moduleArtifact.getModule() == null) { // 149425
			EclipseUtil.openError(Messages.errorNoModules);
			if (Trace.FINEST) {
				Trace.trace(Trace.STRING_FINEST, "Module artifact not contained in a module");
			}
			return;
		}
		final IModule module = moduleArtifact.getModule();
		
		// check for servers with the given start mode
		IServer[] servers = ServerCore.getServers();
		boolean found = false;
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size && !found; i++) {
				if (ServerUIPlugin.isCompatibleWithLaunchMode(servers[i], launchMode)) {
					try {
						IModule[] parents = servers[i].getRootModules(module, null);
						if (parents != null && parents.length > 0)
							found = true;
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}
		
		if (!found) {
			// no existing server supports the project and start mode!
			// check if there might be another one that can be created
			IServerType[] serverTypes = ServerCore.getServerTypes();
			if (serverTypes != null) {
				int size = serverTypes.length;
				for (int i = 0; i < size && !found; i++) {
					IServerType type = serverTypes[i];
					IModuleType[] moduleTypes = type.getRuntimeType().getModuleTypes();
					if (type.supportsLaunchMode(launchMode) && ServerUtil.isSupportedModule(moduleTypes, module.getModuleType())) {
						found = true;
					}
				}
			}
			if (!found) {
				EclipseUtil.openError(Messages.errorNoServer);
				if (Trace.FINEST) {
					Trace.trace(Trace.STRING_FINEST, "No server for start mode");
				}
				return;
			}
		}
		
		if (!ServerUIPlugin.saveEditors())
			return;

		tasksAndClientShown = false;
		IServer server2 = null;
		// initialize its value using the predefined value if one has been given
		client = (IClient)getOverwriteValue(ROS_CLIENT);
		launchableAdapter = (ILaunchableAdapter) getOverwriteValue(ROS_LAUNCHABLE);
		
		if (Trace.FINEST) {
			Trace.trace(Trace.STRING_FINEST, 
					"Client and launchableAdapter after setting predefined values: launchableAdapter="
					+ launchableAdapter + " client=" + client);
		}		
		
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			server2 = getServer(module, moduleArtifact, monitor);
			if (monitor.isCanceled())
				return;
			
			if (server2 != null) {
				IFolder folder = server2.getServerConfiguration();
				if (folder != null && folder.getProject() != null && !folder.getProject().isOpen())
					folder.getProject().open(monitor);
			}
		} catch (CoreException ce) {
			EclipseUtil.openError(shell, ce.getLocalizedMessage());
			return;
		}
		final IServer server = server2;
		//if (monitor.isCanceled())
		//	return;
		
		if (Trace.FINEST) {
			Trace.trace(Trace.STRING_FINEST, "Server: " + server);
		}
		
		if (server == null) {
			EclipseUtil.openError(Messages.errorNoServer);
			if (Trace.SEVERE) {
				Trace.trace(Trace.STRING_SEVERE, "No server found");
			}
			return;
		}
		
		if (!ServerUIPlugin.promptIfDirty(shell, server))
			return;
		
		// We need to check if the client and launchable were pre-populated
		if (!tasksAndClientShown) {
			RunOnServerWizard wizard = new RunOnServerWizard(server, launchMode, moduleArtifact, wiz_properties);
			if (wizard.shouldAppear()) {
				WizardDialog dialog = new WizardDialog(shell, wizard);
				if (dialog.open() == Window.CANCEL)
					return;
			} else
				wizard.performFinish();

			// Do not overwrite the client or launchableAdapter value, as it may
			// have been set by getOverwriteValue, which will add predefined values 
			// if provided. There is no guarantee that getting the values (client and 
			// launchableAadapter) from the wizard will be valid, since the values from
			// the wizard are used only if the client and launchableAdapter are null
			if (client == null){
				client = wizard.getSelectedClient();
			}
			if (launchableAdapter == null){
				launchableAdapter = wizard.getLaunchableAdapter();
			}
		}
		
		// if there is no client, use a dummy
		if (client == null) {		
			client = new IClient() {
				public String getDescription() {
					return Messages.clientDefaultDescription;
				}

				public String getId() {
					return "org.eclipse.wst.server.ui.client.default";
				}

				public String getName() {
					return Messages.clientDefaultName;
				}

				public IStatus launch(IServer server3, Object launchable2, String launchMode3, ILaunch launch) {
					return Status.OK_STATUS;
				}

				public boolean supports(IServer server3, Object launchable2, String launchMode3) {
					return true;
				}
			};
		}
		
		if (Trace.FINEST) {
			Trace.trace(Trace.STRING_FINEST, 
					"Prior to creating launch client jobs: launchableAdapter="+ launchableAdapter + " client=" + client);
		}
		
		if (moduleArtifact instanceof ModuleArtifactDelegate) {
			boolean canLoad = false;
			try {
				Class c = Class.forName(moduleArtifact.getClass().getName());
				if (c.newInstance() != null)
					canLoad = true;
			} catch (Throwable t) {
				if (Trace.WARNING) {
					Trace.trace(Trace.STRING_WARNING,
							"Could not load module artifact delegate class, switching to backup");
				}
			}
			if (canLoad) {
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					ILaunchConfiguration config = getLaunchConfiguration(server, (ModuleArtifactDelegate) moduleArtifact, launchableAdapter, client, monitor);
					config.launch(launchMode, monitor);
				} catch (CoreException ce) {
					if (Trace.SEVERE) {
						Trace.trace(Trace.STRING_SEVERE, "Could not launch Run on Server", ce);
					}
				}
				return;
			}
		}
		
		Thread thread = new Thread("Run on Server") {
			public void run() {
				if (Trace.FINEST) {
					Trace.trace(Trace.STRING_FINEST, "Ready to launch");
				}
				
				// start server if it's not already started
				// and cue the client to start
				IModule[] modules = new IModule[] { module }; // TODO: get parent hierarchy correct
				int state = server.getServerState();
				if (state == IServer.STATE_STARTING) {
					LaunchClientJob clientJob = new LaunchClientJob(server, modules, launchMode, moduleArtifact, launchableAdapter, client);
					clientJob.schedule();
				} else if (state == IServer.STATE_STARTED) {
					boolean restart = false;
					String mode = server.getMode();
					IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
					boolean disabledBreakpoints = false;
					
					if (server.getServerRestartState()) {
						int result = openRestartDialog(shell);
						if (result == 0) {
							launchMode = mode;
							restart = true;
						} else if (result == 9) // cancel
							return;
					}
					if (!restart) {
						if (!ILaunchManager.RUN_MODE.equals(mode) && ILaunchManager.RUN_MODE.equals(launchMode)) {
							boolean breakpointsOption = false;
							if (breakpointManager.isEnabled() && ILaunchManager.DEBUG_MODE.equals(mode))
								breakpointsOption = true;
							int result = openOptionsDialog(shell, Messages.wizRunOnServerTitle, Messages.dialogModeWarningRun, breakpointsOption);
							if (result == 0)
								restart = true;
							else if (result == 1) {
								breakpointManager.setEnabled(false);
								disabledBreakpoints = true;
								launchMode = mode;
							} else if (result == 2)
								launchMode = mode;
							else // result == 9 // cancel
								return;
						} else if (!ILaunchManager.DEBUG_MODE.equals(mode) && ILaunchManager.DEBUG_MODE.equals(launchMode)) {
							int result = openOptionsDialog(shell, Messages.wizDebugOnServerTitle, Messages.dialogModeWarningDebug, false);
							if (result == 0)
								restart = true;
							else if (result == 1)
								launchMode = mode;
							else // result == 9 // cancel
								return;
						} else if (!ILaunchManager.PROFILE_MODE.equals(mode) && ILaunchManager.PROFILE_MODE.equals(launchMode)) {
							boolean breakpointsOption = false;
							if (breakpointManager.isEnabled() && ILaunchManager.DEBUG_MODE.equals(mode))
								breakpointsOption = true;
							int result = openOptionsDialog(shell, Messages.wizProfileOnServerTitle, Messages.dialogModeWarningProfile, breakpointsOption);
							if (result == 0)
								restart = true;
							else if (result == 1) {
								breakpointManager.setEnabled(false);
								disabledBreakpoints = true;
								launchMode = mode;
							} else if (result == 2)
								launchMode = mode;
							else // result == 9 // cancel
								return;
						}
						
						if (ILaunchManager.DEBUG_MODE.equals(launchMode)) {
							if (!breakpointManager.isEnabled() && !disabledBreakpoints) {
								int result = openBreakpointDialog(shell);
								if (result == 0)
									breakpointManager.setEnabled(true);
								else if (result == 1) {
									// ignore
								} else // result == 2
									return;
							}
						}
					}
					
					final LaunchClientJob clientJob = new LaunchClientJob(server, modules, launchMode, moduleArtifact, launchableAdapter, client);
					if (restart) {
						final IServer server3 = server;
						server.restart(launchMode, new IServer.IOperationListener() {
							public void done(IStatus result) {
								// Only publish if the server requires publish before launching the client.
								if (server3.shouldPublish()) {
									server3.publish(IServer.PUBLISH_INCREMENTAL, null, info, new IServer.IOperationListener() {
										public void done(IStatus result2) {
											if (result2.isOK())
												clientJob.schedule();
										}
									});
								} else {
									clientJob.schedule();
								}
							}
						});
					} else {
						// Only publish if the server requires publish before launching the client.
						if (server.shouldPublish()) {
							server.publish(IServer.PUBLISH_INCREMENTAL, null, info, new IServer.IOperationListener() {
								public void done(IStatus result) {
									if (result.isOK())
										clientJob.schedule();
								}
							});
						} else {
							clientJob.schedule();
						}
					}
				} else if (state != IServer.STATE_STOPPING) {
					final LaunchClientJob clientJob = new LaunchClientJob(server, modules, launchMode, moduleArtifact, launchableAdapter, client);
					
					server.start(launchMode, new IServer.IOperationListener() {
						public void done(IStatus result) {
							if (result.isOK())
								clientJob.schedule();
						}
					});
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	
}