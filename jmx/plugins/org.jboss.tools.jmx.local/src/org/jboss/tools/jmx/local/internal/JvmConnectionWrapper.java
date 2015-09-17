/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.local.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.common.jdt.debug.RemoteDebugActivator;
import org.jboss.tools.common.jdt.debug.VmModel;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IDebuggableConnection;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmFacade;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.ui.JvmMonitorPreferences;


/**
 * This class needs to be abstracted out with extension points so that
 * custom launches can determine how their jvm appears. 
 * 
 */
public class JvmConnectionWrapper implements IConnectionWrapper, 
	IAdaptable, IJvmFacade, IDebuggableConnection {
	private IActiveJvm activeJvm;
	private JvmKey key;
	private Root root;
	private List<Runnable> afterLoadRunnables = new ArrayList<Runnable>();
        private IProgressMonitor progressMonitor;


	public JvmConnectionWrapper(IActiveJvm vm) {
		this.activeJvm = vm;
		this.progressMonitor = null;
		this.key = getJvmKey(vm);
	}

	@Override
	public String toString() {
		return getName();
	}

	public IActiveJvm getActiveJvm() {
		return activeJvm;
	}

	public void setActiveJvm(IActiveJvm activeJvm) {
		if (this.activeJvm != activeJvm) {
			IActiveJvm oldJvm = this.activeJvm;
			this.activeJvm = activeJvm;
			this.key = getJvmKey(activeJvm);
			if (oldJvm != null) {
				try {
					oldJvm.disconnect();
				} catch (Throwable t) {
					// ignore
				}
			}
		}
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		ITabbedPropertySheetPageContributor contributor = new ITabbedPropertySheetPageContributor() {
			public String getContributorId() {
				return "org.jboss.tools.jmx.jvmmonitor.ui.JvmExplorer";
			}
		};
		if (adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(contributor);
		} else if (adapter == ITabbedPropertySheetPageContributor.class) {
			return contributor;
		}
		return null;
	}

	protected void addOnLoadRunnable(Runnable runnable) {
		afterLoadRunnables.add(runnable);
	}

	public MBeanServerConnection getConnection() {
		return activeJvm.getMBeanServer().getConnection();
	}

	public synchronized void connect() throws IOException {
		if (!activeJvm.isConnected() && activeJvm.isConnectionSupported()) {
			int updatePeriod = JvmMonitorPreferences.getJvmUpdatePeriod();

			try {
				activeJvm.connect(updatePeriod);
			} catch (JvmCoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fireConnectionChanged();
		}
	}

	public synchronized void disconnect() throws IOException {
		root = null;
		activeJvm.disconnect();
		fireConnectionChanged();
	}

	public boolean isConnected() {
		return activeJvm.isConnected();
	}

	public Root getRoot() {
		return root;
	}

	public void loadRoot() {
	    if (isConnected() && root == null) {
		try {
		    if (progressMonitor == null)
			progressMonitor = new NullProgressMonitor();

		    root = NodeUtils.createObjectNameTree(this, progressMonitor);
		    for (Runnable task : afterLoadRunnables) {
		    	task.run();
		    }
		    afterLoadRunnables.clear();
		} catch (Throwable e) {
		    Activator.pluginLog().logWarning("Failed to load JMX tree for " + this + ". " + e, e);
		}
		progressMonitor = null;
	    }
	}

	public void run(IJMXRunnable runnable) throws JMXException {
		try {
			runnable.run(getConnection());
		} catch (Exception ce) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID,
					JMXCoreMessages.DefaultConnection_ErrorRunningJMXCode, ce);
			throw new JMXException(s);
		}
	}

	@Override
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs)
			throws JMXException {
		run(runnable);
	}
	
	public boolean canControl() {
		return true;
	}

	protected void fireConnectionChanged() {
		JVMConnectionProvider provider = (JVMConnectionProvider) getProvider();
		provider.fireChanged(this);
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JVMConnectionProvider.PROVIDER_ID);
	}
	
	public String getName() {
		return activeJvm == null ? "null" : activeJvm.getMainClass();
	}
	

	public Properties getAgentProperties() {
		/*
		try {
			return vm.getAgentProperties();
		} catch (IOException e) {
			JMXUIActivator.getLogger().warning("Failed to get Agent Properties: " + e, e);
		}
		 */
		return new Properties();
	}

	public Map<String,String> getSystemProperties() {
		return System.getenv();
	}


	@Override
	public void loadRoot(IProgressMonitor monitor) throws CoreException {
	    progressMonitor = monitor;
	    loadRoot();
	}
	
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JvmConnectionWrapper && key.equals(((JvmConnectionWrapper)obj).getKey());
	}

	protected JvmKey getKey() {
		return key;
	}
	
	
	private JvmKey getJvmKey(IActiveJvm jvm) {
		int pid = jvm.getPid();
		String hostName = jvm.getHost().getName();
		return new JvmKey(hostName, pid, jvm);
	}

	
	
	/*
	 * For Connecting debugger integration
	 */
	private VmModel vmModel;
	private VmModel getVmModel() {
		if( vmModel == null ) {
			if( activeJvm != null ) {
				String hostname = activeJvm.getHost().getName();
				int pid = activeJvm.getPid();
				VmModel model2 = RemoteDebugActivator.getDefault().getCachedVmModel(hostname, pid);
				if( model2 != null ) {
					if( RemoteDebugActivator.getDefault().isDebugModel(model2)) {
						vmModel = model2;
					} else if(model2.getMainClass() == null ) {
						// No main class, may be a suspended process
						vmModel = model2;
					}
				}
			}
		}
		return vmModel;
	}
	
	@Override
	public boolean debugEnabled() {
		return getVmModel() != null;
	}

	@Override
	public String getDebugHost() {
		return activeJvm == null ? null : activeJvm.getHost().getName();
	}

	@Override
	public int getDebugPort() {
		VmModel m = getVmModel();
		if( m != null ) {
			String debugPort = m.getDebugPort();
			if( debugPort != null )
				return Integer.parseInt(debugPort);
		}
		return -1;
	}

	@Override
	public String getMainClass() {
		VmModel m = getVmModel();
		if( m != null) {
			return m.getMainClass();
		}
		return null;
	}

}
