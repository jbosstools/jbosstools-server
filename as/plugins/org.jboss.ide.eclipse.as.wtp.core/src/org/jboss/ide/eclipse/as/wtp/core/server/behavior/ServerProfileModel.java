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
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Trace;
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
 */
public class ServerProfileModel {
	private static final String SERVER_PROFILE_PROPERTY_KEY = "org.jboss.ide.eclipse.as.core.server.serverMode"; //$NON-NLS-1$
	private static final String LEGACY_MODE_KEY = IDeployableServer.SERVER_MODE;
	
	/**
	 * This is the default profile name for any and all server types that use this framework.
	 * However, a server delegate can (and SHOULD) set the profile to a value 
	 * in their setDefaults() method if they have specific profiles they want as the default for 
	 * their server type. 
	 * 
	 * @see org.eclipse.wst.server.core.model.ServerDelegate#setDefaults(IProgressMonitor monitor))
	 */
	public static final String DEFAULT_SERVER_PROFILE = "local";
	
	public static String getProfile(IServerAttributes server) {
		return getProfile(server, DEFAULT_SERVER_PROFILE);
	}

	public static String getProfile(IServerAttributes server, String defaultProfile) {
		return server.getAttribute(SERVER_PROFILE_PROPERTY_KEY, server.getAttribute(LEGACY_MODE_KEY, defaultProfile));
	}

	public static void setProfile(IServerWorkingCopy wc, String profile) {
		wc.setAttribute(SERVER_PROFILE_PROPERTY_KEY, profile);
	}
	
	public static boolean isProfileKey(String key) {
		return SERVER_PROFILE_PROPERTY_KEY.equals(key);
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
		Trace.trace(Trace.STRING_FINER, "Checking if profile " + profile + " for server type " + serverType + " requires a runtime");
		String[] systems = SubsystemModel.getInstance().getAllSystemsForServertype(serverType);
		ServerProfile sp = internal.getProfile(profile, serverType);
		/// get A subsystem mapping for each system type that exists.
		// See if any of those mappings require a runtime. 
		for( int i = 0; i < systems.length; i++ ) {
			try {
				SubsystemMapping oneMapping = sp.getControllerMapping(serverType, systems[i], null);
				if( oneMapping == null ) {
					// Not all possible systems are absolutely required for all profiles. 
					// For example, a server using mgmt mode may not require a filesystem subsystem at all, since it will never use it. 
					oneMapping = SubsystemModel.getInstance().getSubsystemMappingForCreation(serverType, systems[i], null, null, null);
				}
				if( oneMapping != null && oneMapping.getSubsystem() != null ) {
					if(oneMapping.getSubsystem().requiresRuntime()) {
						Trace.trace(Trace.STRING_FINER, "Profile " + profile + " for server type " + serverType + " requires a runtime");
						return true;
					} 
				} else {
					Trace.trace(Trace.STRING_FINER, "Profile " + profile + " for server type " + serverType + " cannot find a subsystem for system " + systems[i]);
				}
			} catch(CoreException ce) {
				Trace.trace(Trace.STRING_FINER, "Non-critical error searching for a subsystem for system " + systems[i] + ", profile " + profile + ", and server type " + serverType);
			}
		}
		Trace.trace(Trace.STRING_FINER, "Profile " + profile + " for server type " + serverType + " does not require a runtime");
		return false;
	}
	
	public ServerProfile[] getProfiles(String serverType) {
		return internal.getProfiles(serverType);
	}
	
	public ServerProfile getProfile(String serverType, String id) {
		Trace.trace(Trace.STRING_FINER, "Locating server profile " + id + " for server type " + serverType);
		ServerProfile[] profiles = internal.getProfiles(serverType);
		if( profiles != null ) {
			for( int i = 0; i < profiles.length;i++) {
				if( profiles[i].getId().equals(id)) {
					Trace.trace(Trace.STRING_FINER, "Server profile " + id + " for server type " + serverType + " found.");
					return profiles[i];
				}
			}
		}
		Trace.trace(Trace.STRING_FINER, "Server profile " + id + " for server type " + serverType + " not found.");
		return null;
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
		Trace.trace(Trace.STRING_FINER, "Locating subsystem controller for profile " + profile + " for server type " + serverType + " and system " + system);
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
		private String visibleName, description;
		
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
		
		protected void setDescription(String desc) {
			this.description = desc;
		}
		protected void setVisibleName(String name) {
			this.visibleName = name;
		}
		public String getDescription() {
			return this.description;
		}
		public String getVisibleName() {
			return this.visibleName;
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
			
			IConfigurationElement[] description = el.getChildren("description");
			if( description.length > 0 ) {
				if( description.length > 1 || this.description != null || this.visibleName != null) {
					String id = el.getAttribute("id");
					throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,
							"Multiple descriptions declared for profile id " + id + " and server type " + serverType));
				}
				String name = description[0].getAttribute("name");
				String desc = description[0].getAttribute("description");
				setVisibleName(name);
				setDescription(desc);
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
			addSystemMapping(system, subsystem);
		}
		
		private void addSystemMapping(String system, String subsystem) throws CoreException {
			//System.out.println("Adding " + system + " -> " + subsystem + " for server " + serverType + " with profile " + id);
			String val = subsystems.get(system);
			if( val != null )
				throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Multiple defaults set for profile " + id + " and system " + system + ". ids: " + subsystem + " and " + val));
			subsystems.put(system,  subsystem);
		}
		
		public ISubsystemController getController(IServer server, String system, ControllerEnvironment environment) throws CoreException {
			return getController(server, server.getServerType().getId(), system, environment);
		}
		
		public ISubsystemController getController(IServer server, String serverType, String system, ControllerEnvironment environment) throws CoreException {
			Map<String,Object> envMap =  environment == null ? null : environment.getMap();
			String subsystem = subsystems.get(system);
			if( subsystem != null )
				return SubsystemModel.getInstance().createControllerForSubsystem(
					server, serverType, system, subsystem, envMap);
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
