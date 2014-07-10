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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.HasName;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.providers.DefaultConnectionProvider;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmFacade;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.ui.JvmMonitorPreferences;
import org.jboss.tools.jmx.ui.ImageProvider;


/**
 * This class needs to be abstracted out with extension points so that
 * custom launches can determine how their jvm appears. 
 * 
 */
public class JvmConnectionWrapper implements IConnectionWrapper, HasName, ImageProvider, IAdaptable, IJvmFacade {
	private static final String MAVEN_PREFIX = "org.codehaus.plexus.classworlds.launcher.Launcher";
	private static final String ECLIPSE_MAVEN_PROCESS_PREFIX  = "-DECLIPSE_PROCESS_NAME='";
	private static final String ECLIPSE_MAVEN_PROCESS_POSTFIX = "'";
	private static final String KARAF_HOME_PREFIX = " -Dkaraf.home=";
	private static final String KARAF_HOME_POSTFIX = " ";

	protected static final Map<String,String> vmAliasMap = new HashMap<String, String>();
	protected static final Map<String,String> karafSubTypeMap = new HashMap<String, String>();
	
	private IActiveJvm activeJvm;
	private String name;
	private Root root;
	private Image image;
	private List<Runnable> afterLoadRunnables = new ArrayList<Runnable>();
        private IProgressMonitor progressMonitor;

	static {
		vmAliasMap.put("com.intellij.rt.execution.application.AppMain", "idea");
		vmAliasMap.put("org.apache.karaf.main.Main", "karaf");
		vmAliasMap.put("org.eclipse.equinox.launcher.Main", "equinox");
		vmAliasMap.put("org.jetbrains.idea.maven.server.RemoteMavenServer", "idea maven server");
		vmAliasMap.put("idea maven server", "");
		vmAliasMap.put("scala.tools.nsc.MainGenericRunner", "scala repl");

		karafSubTypeMap.put("default", "Apache Karaf");
		karafSubTypeMap.put("esb-version.jar", "JBoss Fuse");
		karafSubTypeMap.put("fabric-version.jar", "Fabric8");
		karafSubTypeMap.put("mq-version.jar", "JBoss A-MQ");
		karafSubTypeMap.put("servicemix-version.jar", "Apache ServiceMix");
	}

	public JvmConnectionWrapper(IActiveJvm vm) {
		this.activeJvm = vm;
		this.progressMonitor = null;
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

	public boolean canControl() {
		return true;
	}

	protected void fireConnectionChanged() {
		DefaultConnectionProvider provider = (DefaultConnectionProvider) getProvider();
		provider.fireChanged(this);
	}

	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(DefaultConnectionProvider.PROVIDER_ID);
	}

	/**
	 * Returns true if this process is a karaf container 
	 */
	private static boolean isKaraf(IActiveJvm jvm) {
		String displayName = jvm.getMainClass();
		return equal("org.apache.karaf.main.Main", displayName);
	}
	
	private static boolean equal(Object a, Object b) {
		if (a == b) {
			return true;
		}
		return a != null && b != null && a.equals(b);
	}

	private static boolean isBlank(String text) {
		return text == null || text.trim().length() == 0;
	}
	
	private abstract static class JvmConnectionWrapperLabelProvider {
		public abstract boolean accepts(IActiveJvm jvm);
		public abstract Image getImage(IActiveJvm jvm);
		public abstract String getDisplayString(IActiveJvm jvm);
	}
	
	private JvmConnectionWrapperLabelProvider[] jvmConnectionLabelProviders = new JvmConnectionWrapperLabelProvider[]{
			new CamelContextLabelProvider(),
			new MavenLabelProvider(),
			new KarafLabelProvider(),
			new JavaProcessLabelProvider()
	};
	
	private static class JavaProcessLabelProvider extends JvmConnectionWrapperLabelProvider {
		public boolean accepts(IActiveJvm jvm) {
			// checked last, so always accepts
			return true;
		}
		public Image getImage(IActiveJvm jvm) {
			return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
		}
		public String getDisplayString(IActiveJvm jvm) {
			if( isBlank(jvm.getMainClass())) 
				return "Java Process";
			String main = jvm.getMainClass();
			String alias = vmAliasMap.get(main);
			return alias == null ? main : alias;
		}
	}

	private static class CamelContextLabelProvider extends JvmConnectionWrapperLabelProvider {
		public boolean accepts(IActiveJvm jvm) {
			return jvm.getMainClass().endsWith("org.apache.camel:camel-maven-plugin:run");
		}
		public Image getImage(IActiveJvm jvm) {
			return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CAMEL_PNG);
		}
		public String getDisplayString(IActiveJvm jvm) {
			return "Local Camel Context";
		}
	}

	private static class MavenLabelProvider extends JvmConnectionWrapperLabelProvider {
		public boolean accepts(IActiveJvm jvm) {
			return jvm.getMainClass().startsWith(MAVEN_PREFIX);
		}
		public Image getImage(IActiveJvm jvm) {
			return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
		}
		public String getDisplayString(IActiveJvm jvm) {
			String displayName = "maven" + jvm.getMainClass().substring(MAVEN_PREFIX.length());
			if (!jvm.isRemote()) {
				String pInfo = ProcessInformationStore.getDefault().queryProcessInformation(jvm.getPid());
				if (pInfo != null) {
					int start = pInfo.indexOf(ECLIPSE_MAVEN_PROCESS_PREFIX);
					if (start != -1) {
						int end   = pInfo.indexOf(ECLIPSE_MAVEN_PROCESS_POSTFIX, start+ECLIPSE_MAVEN_PROCESS_PREFIX.length()+1);
						if (end != -1) {
							displayName = pInfo.substring(start + ECLIPSE_MAVEN_PROCESS_PREFIX.length(), end);
						} else {
							displayName = pInfo.substring(start + ECLIPSE_MAVEN_PROCESS_PREFIX.length());
						}
					}
				}
			}
			return displayName;
		}
	}
	
	private static class KarafLabelProvider extends JvmConnectionWrapperLabelProvider {
		public boolean accepts(IActiveJvm jvm) {
			return isKaraf(jvm);
		}
		public Image getImage(IActiveJvm jvm) {
			String karafHomeFolder = getKarafHomeFolder(jvm);
			String karafSubType = getKarafSubtype(karafHomeFolder);
			Image i = null;
			if (karafSubType != null) {
				if (karafSubType.toLowerCase().contains("esb")) {
					i = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.FUSE_PNG);
				} else if (karafSubType.toLowerCase().contains("fabric")) {
					i = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.FABRIC_PNG);
				} else if (karafSubType.toLowerCase().contains("mq")) {
					i = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.MQ_PNG);
				} else if (karafSubType.toLowerCase().contains("servicemix")) {
					i = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.SMX_PNG);
				} else {
					i = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_PNG);
				}
			}
			return i;
		}
		public String getDisplayString(IActiveJvm jvm) {
			String karafHomeFolder = getKarafHomeFolder(jvm);
			String karafSubType = getKarafSubtype(karafHomeFolder);
			String displayName = jvm.getMainClass();
			if (karafSubType == null) {
				displayName = getNameFromAliasMap(displayName);
			} else {
				displayName = karafSubType;
			}
			return displayName;
		}
	}
	
	
	
	private JvmConnectionWrapperLabelProvider findProvider(IActiveJvm jvm) {
		for( int i = 0; i < jvmConnectionLabelProviders.length; i++ ) {
			if( jvmConnectionLabelProviders[i].accepts(jvm))
				return jvmConnectionLabelProviders[i];
		}
		return null;
	}
	
	public String getName() {
		if (name == null) {
			JvmConnectionWrapperLabelProvider provider  = findProvider(activeJvm);
			String displayName;
			if( provider != null ) {
				displayName = provider.getDisplayString(activeJvm);
			} else {
				displayName = activeJvm.getMainClass();
				displayName = getNameFromAliasMap(displayName);
			}
			// include pid in name
			displayName += " [" + activeJvm.getPid() + "]";
			name = displayName;
		}
		return name;
	}
	
	private static String getKarafHomeFolder(IActiveJvm jvm) {
		String karafHomeFolder = null;
		if (!jvm.isRemote()) {
			String pInfo = ProcessInformationStore.getDefault().queryProcessInformation(jvm.getPid());
			if (pInfo != null) {
				int start = pInfo.indexOf(KARAF_HOME_PREFIX);
				if (start != -1) {
					int end   = pInfo.indexOf(KARAF_HOME_POSTFIX, start+KARAF_HOME_PREFIX.length()+1);
					if (end != -1) {
						karafHomeFolder = pInfo.substring(start + KARAF_HOME_PREFIX.length(), end);
					}
				}
			}
		}
		return karafHomeFolder;
	}
	private static String getKarafSubtype(String karafHomeFolder) {
		String karafSubType = null;
		if (karafHomeFolder != null) {
			File libFolder = new File(String.format("%s%slib%s", karafHomeFolder, File.separator, File.separator));
			if (libFolder.exists() && libFolder.isDirectory()) {
				File[] jars = libFolder.listFiles(new FileFilter() {
					public boolean accept(File f) {
						return f.isFile() && (f.getName().toLowerCase().endsWith("-version.jar"));
					}
				});
				if (jars != null && jars.length==1) {
					File f = jars[0];
					if (karafSubTypeMap.containsKey(f.getName())) {
						karafSubType = karafSubTypeMap.get(f.getName());
					} else {
						karafSubType = karafSubTypeMap.get("default");
					}
				}
			}
		}
		return karafSubType;
	}

	private static String getNameFromAliasMap(String displayName) {
		Set<Entry<String, String>> entrySet = vmAliasMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			if (displayName.startsWith(key)) {
				return (entry.getValue() + displayName.substring(key.length()));
			}
		}
		return displayName;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Image getImage() {
		if (image == null) {
			Image ret = null;
			JvmConnectionWrapperLabelProvider provider  = findProvider(activeJvm);
			if( provider != null ) {
				ret = provider.getImage(activeJvm);
			}
			if( ret == null ) {
				ret = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
			}
			image = ret;
		}
		return image;
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
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs)
			throws JMXException {
		// TODO Auto-generated method stub

	}
}
