package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IJBossLaunchDelegate;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;

public class BehaviourModel {
	private static BehaviourModel model;
	public static BehaviourModel getModel() {
		if( model == null ) 
			model = new BehaviourModel();
		return model;
	}
	
	/*
	 * Server type maps to some behaviour
	 */
	private HashMap<String, Behaviour> map;
	public BehaviourModel() {
		map = new HashMap<String, Behaviour>();
		loadModel();
	}
	
	public static IJBossServerPublishMethodType getPublishMethodType(IServer server) {
		String serverType = server.getServerType().getId();
		String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(server);
		return BehaviourModel.getModel().getBehaviour(serverType).getImpl(behaviourType);
	}
	
	/*
	 * Get all setup participants for this server type, regardless
	 * of behaviour type
	 */
	public ArrayList<IJBossLaunchDelegate> getSetupParticipants(IServer server) {
		Behaviour beh = map.get(server.getServerType().getId());
		ArrayList<IJBossLaunchDelegate> list = new ArrayList<IJBossLaunchDelegate>();
		BehaviourImpl[] impls = beh.getImplementations();
		for( int i = 0; i < impls.length; i++ ) {
			list.add(impls[i].getLaunchDelegate());
		}
		return list;
	}
	
	/*
	 * Get the single launch delegate designated for this server type
	 */
	public IJBossLaunchDelegate getLaunchDelegate(IServer server, String mode) {
		Behaviour beh = map.get(server.getServerType().getId());
		return beh.getImpl(mode).getLaunchDelegate();
	}

	protected void loadModel() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "behaviourExtension"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			String serverTypes = cf[i].getAttribute("serverTypes"); //$NON-NLS-1$
			String[] allTypes = serverTypes.split(","); //$NON-NLS-1$
			BehaviourImpl impl = new BehaviourImpl(cf[i]);
			Behaviour b;
			for( int j = 0; j < allTypes.length; j++ ) {
				b = getOrCreateBehaviour(allTypes[j]);
				b.addImpl(impl);
			}
		}
	}
	
	public Behaviour getBehaviour(String serverType) {
		return map.get(serverType);
	}
	
	protected Behaviour getOrCreateBehaviour(String serverType) {
		Behaviour b = map.get(serverType);
		if( b == null )
			map.put(serverType, new Behaviour(serverType));
		return map.get(serverType);
	}
	
	public static class Behaviour {
		private String serverType;
		private HashMap<String, BehaviourImpl> behTypeToImpl;
		public Behaviour(String serverType) {
			this.serverType = serverType;
			behTypeToImpl = new HashMap<String, BehaviourImpl>();
		}
		public String getServerType() {
			return serverType;
		}
		// Will overwrite previous values
		public void addImpl(BehaviourImpl impl) {
			behTypeToImpl.put(impl.typeId, impl);
		}
		public BehaviourImpl getImpl(String behaviourType) {
			return behTypeToImpl.get(behaviourType);
		}
		public BehaviourImpl[] getImplementations() {
			ArrayList<BehaviourImpl> list = new ArrayList<BehaviourImpl>();
			list.addAll(behTypeToImpl.values());
			return list.toArray(new BehaviourImpl[list.size()]);
		}
		
		public String[] getSupportedBehaviours() {
			Set<String> c = behTypeToImpl.keySet();
			ArrayList<String> c2 = new ArrayList<String>();
			c2.addAll(c);
			Collections.sort(c2);
			return c2.toArray(new String[c2.size()]);
		}
	}
	
	/*
	 * This "implements" is a hack, just trying to get shit to compile! 
	 */
	public static class BehaviourImpl implements IJBossServerPublishMethodType {
		private String name;
		private String typeId;
		private IConfigurationElement element;
		private IJBossLaunchDelegate launchDelegate;
		private String supportedServers;
		private IJBossBehaviourDelegate behaviourDelegate = null;
		
		public BehaviourImpl(IConfigurationElement element) {
			this.element = element;
			name = element.getAttribute("name"); //$NON-NLS-1$
			typeId = element.getAttribute("typeId");//$NON-NLS-1$
			supportedServers = element.getAttribute("serverTypes"); //$NON-NLS-1$
		}
		
		public String getName() {
			return name;
		}
		
		public String getId() {
			return typeId;
		}
		public boolean accepts(String serverTypeId) {
			String[] servers = supportedServers.split(","); //$NON-NLS-1$
			for( int i = 0; i < servers.length; i++ ) 
				if( servers[i].trim().equals(serverTypeId))
					return true;
			return false;
		}
		
		public IJBossServerPublishMethod createPublishMethod() {
			try {
				return (IJBossServerPublishMethod) element.createExecutableExtension("publishMethod"); //$NON-NLS-1$
			} catch( CoreException ce ) {
				JBossServerCorePlugin.getInstance().getLog().log(ce.getStatus());
			}
			return null;
		}
		
		public IJBossLaunchDelegate getLaunchDelegate() {
			if( launchDelegate != null)
				return launchDelegate;
			
			try {
				launchDelegate = (IJBossLaunchDelegate) element.createExecutableExtension("launchDelegate"); //$NON-NLS-1$
				return launchDelegate;
			} catch( CoreException ce ) {
				JBossServerCorePlugin.getInstance().getLog().log(ce.getStatus());
			}
			return null;
		}
		
		public IJBossBehaviourDelegate getBehaviourDelegate() {
			if( behaviourDelegate != null )
				return behaviourDelegate;
			try {
				behaviourDelegate = (IJBossBehaviourDelegate)element.createExecutableExtension("behaviourDelegate"); //$NON-NLS-1$
				return behaviourDelegate;
			} catch(CoreException ce) {
				JBossServerCorePlugin.getInstance().getLog().log(ce.getStatus());
			}
			return null;
		}
	}
}
