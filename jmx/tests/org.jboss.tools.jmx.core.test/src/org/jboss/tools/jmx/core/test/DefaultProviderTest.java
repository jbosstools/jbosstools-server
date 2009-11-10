package org.jboss.tools.jmx.core.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.providers.DefaultConnectionProvider;
import org.jboss.tools.jmx.core.test.util.TestProjectProvider;
import org.jboss.tools.jmx.core.tree.DomainNode;
import org.jboss.tools.jmx.core.tree.Node;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.test.util.JobUtils;

public class DefaultProviderTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testExtensionExists() {
    	String providerClass = "org.jboss.tools.jmx.core.providers.DefaultConnectionProvider";
		IExtension[] extensions = findExtension(ExtensionManager.MBEAN_CONNECTION);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i]
					.getConfigurationElements();
			for( int j = 0; j < elements.length; j++ ) {
				if( elements[j].getAttribute("class").equals(providerClass))
					return;
			}
		}
		fail("Default Provider extension not found");
	}

	public void testProviderExists() throws Exception {
		IConnectionProvider defProvider = null;
		IConnectionProvider[] providers = ExtensionManager.getProviders();
		for( int i = 0; i < providers.length; i++ ) {
			if( providers[i].getId().equals(DefaultConnectionProvider.PROVIDER_ID))
				defProvider = providers[i];
		}
		if( defProvider == null )
			fail("Default Provider not found");

		defProvider = ExtensionManager.getProvider(DefaultConnectionProvider.PROVIDER_ID);
		if( defProvider == null )
			fail("Default Provider not found 2");

    }

	@SuppressWarnings("unchecked")
	public void testConnection() throws Exception {
		TestProjectProvider projectProvider;
		IProject project;
		projectProvider = new TestProjectProvider(JMXTestPlugin.PLUGIN_ID,
				"projects" + Path.SEPARATOR + "JMX_EXAMPLE",
				null, true);
		project = projectProvider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		JobUtils.waitForIdle();
		
		ILaunchConfigurationWorkingCopy wc = createLaunch();
		ILaunch launch = wc.launch("run", new NullProgressMonitor());


		/* */
		IProcess p = launch.getProcesses()[0];
		p.getStreamsProxy().getErrorStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				System.out.println("[error] " + text);
			} 
		});
		p.getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				System.out.println("[out] " + text);
			} 
		});
		
		 /* */
		Thread.sleep(10000);
		
		try {
			IConnectionProvider defProvider =
				ExtensionManager.getProvider(DefaultConnectionProvider.PROVIDER_ID);
			HashMap map = new HashMap();
			map.put(DefaultConnectionProvider.ID, "Test Connection");
			map.put(DefaultConnectionProvider.URL, "service:jmx:rmi:///jndi/rmi://localhost:9999" +
					"/jmxrmi");
			map.put(DefaultConnectionProvider.USERNAME, "");
			map.put(DefaultConnectionProvider.PASSWORD, "");
			IConnectionWrapper wrapper = defProvider.createConnection(map);
			assertTrue("Connection was null", wrapper != null);

			wrapper.connect();
			Root root = wrapper.getRoot();
			assertTrue("Root was not null", root == null);
			
			wrapper.loadRoot(new NullProgressMonitor());
			root = wrapper.getRoot();
			assertTrue("Root was null", root != null);
			
			Node[] children = root.getChildren();
			assertTrue("children were null", children != null);
			assertTrue("children length was less than 1", children.length >= 0);
			
			boolean found = false;
			for( int i = 0; i < children.length; i++ )
				if( children[i] instanceof DomainNode && ((DomainNode)children[i]).getDomain().equals("com.example.mbeans"))
					found = true;
			
			assertTrue("Domain \"com.example\" not found", found);
		} finally {
 			projectProvider.dispose();
			launch.terminate();
		}
	}

	@SuppressWarnings("unchecked")
	protected ILaunchConfigurationWorkingCopy createLaunch() throws Exception {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, "Test1");

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "JMX_EXAMPLE");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.example.mbeans.Main");
		wc.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS",
				new ArrayList(Arrays.asList(new String[] {
						"/JMX_EXAMPLE/src/com/example/mbeans/Main.java"
				})));
		wc.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES",
				new ArrayList(Arrays.asList(new String[] {"1"})));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				"-Dcom.sun.management.jmxremote.port=9999 " +
				"-Dcom.sun.management.jmxremote.authenticate=false " +
				"-Dcom.sun.management.jmxremote.ssl=false");
		return wc;
	}

	private static IExtension[] findExtension(String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}
}
