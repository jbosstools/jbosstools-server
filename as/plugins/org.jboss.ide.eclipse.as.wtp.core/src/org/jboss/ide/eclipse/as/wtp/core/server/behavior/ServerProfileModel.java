/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel.SubsystemMapping;

/**
 * This is a class responsible for keeping track of profiles as added through the 
 * serverProfile extension point. Each server type has a list of profiles declared for it.
 * Each server+profile combination has a set of subsystems, one implementation for each system, 
 * which it will return when requested. 
 * 
 * It is not required for all subsystems to be declared in a profile.
 * If a system for a given server type does not have a declared subsystem implementation
 * for a given profile, default resolution through the {@link SubsystemModel} will apply. 
 * 
 * The {@link SubsystemModel} will return a relevant subsystem, but may error if
 * either no controller exists for the server type and system combination, 
 * or if multiple such implementations are available and the model does not know how
 * to choose one over another. 
 * 
 * In the event where you have multiple subsystems declared for a given server type, 
 * for example as7 has both a filesystem publish controller and a management publish controller,
 * it is best to declare one implementation in a profile, or mark one as default. 
 * 
 * This class also allows the creation of initialization participants
 * which can help a new server being created to be initialized with relevant data
 * for a given profile. This feature is as of now unused, and is not guaranteed
 * to be supported in any way.  
 * 
 * TODO Complete this implementation. Debatable how to do this and ensure it still works
 * even when not kicked off from the new server wizard.
 * 
 */
public class ServerProfileModel {
	private static final String SERVER_PROFILE_PROPERTY_KEY = "org.jboss.ide.eclipse.as.core.server.serverMode"; //$NON-NLS-1$
	public static final String DEFAULT_SERVER_PROFILE = "local";
	
	public static String getProfile(IServerAttributes server) {
		return getProfile(server, DEFAULT_SERVER_PROFILE);
	}

	public static String getProfile(IServerAttributes server, String defaultProfile) {
		return server.getAttribute(SERVER_PROFILE_PROPERTY_KEY, defaultProfile);
	}

	public static void setProfile(IServerWorkingCopy wc, String profile) {
		wc.setAttribute(SERVER_PROFILE_PROPERTY_KEY, profile);
	}
	
	
	private static ServerProfileModel profileModel;
	public static ServerProfileModel getDefault() {
		if( profileModel == null )
			profileModel = new ServerProfileModel();
		return profileModel;
	}
	
	private InternalProfileModel internal;
	private ServerProfileModel() {
		internal = new InternalProfileModel();
		internal.load();
	}
	
	public boolean profileRequiresRuntime(String serverType, String profile) {
		String[] systems = SubsystemModel.getInstance().getAllSystemsForServertype(serverType);
		ServerProfile sp = internal.getProfile(profile, serverType);
		/// get A subsystem mapping for each system type that exists.
		// See if any of those mappings require a runtime. 
		for( int i = 0; i < systems.length; i++ ) {
			try {
				SubsystemMapping oneMapping = sp.getControllerMapping(serverType, systems[i], null);
				if( oneMapping == null ) {
					oneMapping = SubsystemModel.getInstance().getSubsystemMappingForCreation(serverType, systems[i], null, null, null);
				}
				if( oneMapping != null && oneMapping.getSubsystem() != null ) {
					if(oneMapping.getSubsystem().requiresRuntime()) {
						return true;
					} 
				} else {
					// TODO log
					System.out.println(i + " Error searching for serverType " + serverType + " and system type " + systems[i] + " using profile " + profile);
				}
			} catch(CoreException ce) {
				// Intentionally do not log here. 
				// This method should just return true or false to the best of its ability. 
			}
		}
		return false;
	}
	
	public ServerProfile[] getProfiles(String serverType) {
		return internal.getProfiles(serverType);
	}
	
	public IServerProfileInitializer[] getInitializers(String serverType, String profile) {
		ServerProfile sp = internal.getProfile(profile, serverType);
		return sp == null ? new IServerProfileInitializer[0] : sp.getInitializers();
	}
	
	/**
	 * This method will attempt to load a proper subsystem given the server, profile, and system. 
	 * If it cannot find one, it will use the default resolution of the {@link SubsystemModel}
	 * 
	 * @param server
	 * @param profile
	 * @param system
	 * @param environment
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController getController(IServer server, String profile, String system, ControllerEnvironment environment) throws CoreException {
		String serverType = server.getServerType().getId();
		ServerProfile sp = internal.getProfile(profile, serverType);
		if( sp != null ) {
			try {
				ISubsystemController controller = sp.getController(server, system, environment);
				if( controller != null ) 
					return controller;
			} catch(CoreException ce) {
				ASWTPToolsPlugin.log(ce.getStatus());
			}
		}
		return SubsystemModel.getInstance().createSubsystemController(server, system, environment == null ? null : environment.getMap());
	}
	
	
	private static class InternalProfileModel {
		// Map<ServerType, Map<ProfileId, ServerProfile>>
		private HashMap<String, HashMap<String, ServerProfile>> model = null;
		public InternalProfileModel() {
			super();
		}
		public void load() {
			model = new HashMap<String, HashMap<String,ServerProfile>>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] cf = registry.getConfigurationElementsFor(ASWTPToolsPlugin.PLUGIN_ID, "serverProfile"); //$NON-NLS-1$
			for( int i = 0; i < cf.length; i++ ) {
				String name = cf[i].getName();
				if( "serverProfile".equals(name)) {
					String id = cf[i].getAttribute("id");
					String serverTypes = cf[i].getAttribute("serverTypes");
					String[] typesSplit = serverTypes.split(",");
					for( int j = 0; j < typesSplit.length; j++ ) {
						ServerProfile sp = getOrCreateProfile(id, typesSplit[j]);
						try {
							sp.addConfigurationElement(cf[i]);
						} catch(CoreException ce) {
							// Logging
							ASWTPToolsPlugin.log(ce);
						}
					}
				}
			}
		}
		private ServerProfile getOrCreateProfile(String profile, String serverType) {
			HashMap<String, ServerProfile> m = model.get(serverType);
			if( m == null ) {
				m = new HashMap<String, ServerProfile>();
				model.put(serverType, m);
			}
			ServerProfile p = m.get(profile);
			if( p == null ) {
				p = new ServerProfile(profile, serverType);
				m.put(profile, p);
			}
			return p;
		}
		private ServerProfile getProfile(String profile, String serverType) {
			HashMap<String, ServerProfile> m = model.get(serverType);
			if( m != null ) {
				ServerProfile ret = m.get(profile);
				return ret;
			}
			return null;
		}
		
		private ServerProfile[] getProfiles(String serverType) {
			HashMap<String, ServerProfile> m = model.get(serverType);
			if( m != null ) {
				Collection<ServerProfile> profiles = m.values();
				return profiles.toArray(new ServerProfile[profiles.size()]);
			}
			return new ServerProfile[0];
		}
		
	}
	
	// Represents the data for a server + profile name combination
	// A given profile/server combination can have many initializers
	// ANd should have one subsystem type per system type or this will error
	public static class ServerProfile {
		private HashMap<String, String> subsystems = new HashMap<String, String>();
		private ArrayList<InitializerWrapper> initializers = new ArrayList<InitializerWrapper>();
		
		private String id, serverType;
		public ServerProfile(String id, String serverType) {
			this.id = id;
			this.serverType = serverType;
		}
		
		public String getId() {
			return id;
		}
		public String getServerType() {
			return serverType;
		}
		
		private void addConfigurationElement(IConfigurationElement el) throws CoreException {
			IConfigurationElement[] initializers = el.getChildren("initializer");
			for( int i = 0; i < initializers.length; i++ ) {
				addInitializer(initializers[i]);
			}
			
			IConfigurationElement[] subsystems = el.getChildren("subsystem");
			for( int i = 0; i < subsystems.length; i++ ) {
				addSystemMapping(subsystems[i]);
			}
			
		}
		
		protected IServerProfileInitializer[] getInitializers() {
			ArrayList<IServerProfileInitializer> valid = new ArrayList<IServerProfileInitializer>();
			Iterator<InitializerWrapper> i = initializers.iterator();
			InitializerWrapper wrapper;
			while(i.hasNext()) {
				wrapper = i.next();
				if( wrapper.getInitializer() != null ) {
					valid.add(wrapper.getInitializer());
				}
			}
			return valid.toArray(new IServerProfileInitializer[valid.size()]);
		}
		
		private void addInitializer(IConfigurationElement el) {
			initializers.add(new InitializerWrapper(el));
		}
		
		private void addSystemMapping(IConfigurationElement el) throws CoreException {
			String system = el.getAttribute("system");
			String subsystem = el.getAttribute("subsystem");
			System.out.println("Adding system Mapping for server=" + serverType + ",profile=" + id + ",system=" + system+",subsystem="+subsystem);
			addSystemMapping(system, subsystem);
		}
		
		private void addSystemMapping(String system, String subsystem) throws CoreException {
			String val = subsystems.get(system);
			if( val != null )
				throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Multiple defaults set for profile " + id + " and system " + system + ". ids: " + subsystem + " and " + val));
			subsystems.put(system,  subsystem);
		}
		
		public ISubsystemController getController(IServer server, String system, ControllerEnvironment environment) throws CoreException {
			Map<String,Object> envMap =  environment == null ? null : environment.getMap();
			String subsystem = subsystems.get(system);
			if( subsystem != null )
				return SubsystemModel.getInstance().createControllerForSubsystem(
					server, server.getServerType().getId(), system, subsystem, envMap);
			return null;
		}
		
		public SubsystemMapping getControllerMapping(String serverType, String system, ControllerEnvironment environment) throws CoreException {
			Map<String,Object> envMap =  environment == null ? null : environment.getMap();
			String subsystem = subsystems.get(system);
			if( subsystem != null )
				return SubsystemModel.getInstance().getSubsystemMappingForCreation(
					serverType, system, null, subsystem, envMap);
			return null;
		}

	}
	
	private static class InitializerWrapper {
		private boolean failed = false;
		private IConfigurationElement element;
		private IServerProfileInitializer initializer;
		public InitializerWrapper(IConfigurationElement element) {
			this.element = element;
		}
		public IServerProfileInitializer getInitializer() {
			if( initializer == null && !failed ) {
				try {
					initializer = (IServerProfileInitializer)element.createExecutableExtension("class");
				} catch(CoreException ce) {
					failed = true;
					ASWTPToolsPlugin.log(ce);
				}
			}
			return initializer;
		}
	}
	
}
