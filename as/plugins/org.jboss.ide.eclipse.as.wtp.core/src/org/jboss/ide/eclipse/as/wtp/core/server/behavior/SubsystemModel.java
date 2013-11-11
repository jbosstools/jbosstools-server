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
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

/**
 * @since 3.0
 */
public class SubsystemModel {
	private static SubsystemModel instance;
	public static SubsystemModel getInstance() {
		if( instance == null )
			instance = new SubsystemModel();
		return instance;
	}
		
	/**
	 * A map of the following structure:
	 *    serverTypeId -> HashMap<subsystemId, subsystemType >
	 */
	private Map<String, Map<String, SubsystemType>> model;
	public static class SubsystemType {
		// Are all dependencies available
		private boolean isValid = true;
		private IStatus validationError;
		
		private String system;
		private String[] serverTypes;
		private String id;
		private String name;
		private IConfigurationElement element;
		private String[] requiredSystems;
		private HashMap<String, String> requiredSubsystems;
		private HashMap<String, String> props;
		
		public SubsystemType(IConfigurationElement element) {
			this.element = element;
			name = element.getAttribute("name");
			id = element.getAttribute("id");
			String typesTmp = element.getAttribute("serverTypes");
			serverTypes = typesTmp == null ? new String[0] : typesTmp.split(",");
			system = element.getAttribute("system");
			
			// Calculate dependencies
			Set<String> reqSystems = new HashSet<String>();
			HashMap<String, String> reqSubsystems = new HashMap<String, String>();
			IConfigurationElement[] children = element.getChildren("dependencies");
			for( int i = 0; i < children.length; i++ ) {
				IConfigurationElement[] required = children[i].getChildren("requires");
				for( int j = 0; j < required.length; j++ ) {
					String systemReq = required[j].getAttribute("system");
					String subsystemReq = required[j].getAttribute("subsystem");
					if( systemReq != null ) 
						reqSystems.add(systemReq);
					if( systemReq != null && subsystemReq != null )
						reqSubsystems.put(systemReq, subsystemReq);
				}
			}
			requiredSystems = (String[]) reqSystems.toArray(new String[reqSystems.size()]);
			requiredSubsystems = reqSubsystems;
			
			
			// Store props
			props = new HashMap<String, String>();
			IConfigurationElement[] properties = element.getChildren("properties");
			for( int i = 0; i < properties.length; i++ ) {
				IConfigurationElement[] property = properties[i].getChildren("property");
				for( int j = 0; j < property.length; j++ ) {
					String key = property[j].getAttribute("key");
					String value = property[j].getAttribute("value");
					if( key != null && value != null ) 
						props.put(key, value);
				}
			}
		}
		
		private ISubsystemController createController() throws CoreException {
			return (ISubsystemController)element.createExecutableExtension("class");
		}
		
		public String[] getRequiredSystems() {
			return requiredSystems;
		}
		public Map<String, String> getRequiredSubsystems() {
			return (Map<String, String>)requiredSubsystems.clone();
		}
		
		public String getSystem() {
			return system;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
		public boolean isValid() {
			return isValid;
		}
		public IStatus getValidationError() {
			return validationError;
		}
		
		public String toString() {
			return getSystem() + " - " + getId();
		}
	}
		
	/**
	 * Return only subsystem types for this server for the appropriate system
	 * @param server
	 * @param system
	 * @return
	 */
	protected SubsystemType[] getSubsystemTypes(String serverType, String system) {
		checkLoaded();
		Map<String, SubsystemType> ret = model.get(serverType);
		if( ret != null ) {
			Collection<SubsystemType> ret2 = ret.values();
			// If we're not filtering on a system category, return
			if( system == null ) {
				return (SubsystemType[]) ret2.toArray(new SubsystemType[ret2.size()]);
			}
			
			// otherwise get rid of any that dont match category
			ArrayList<SubsystemType> clone = new ArrayList<SubsystemType>(ret2);
			Iterator<SubsystemType> i = clone.iterator();
			SubsystemType tmp = null;
			while(i.hasNext()) {
				tmp = i.next();
				if( !tmp.system.equals(system)) {
					i.remove();
				}
			}
			return (SubsystemType[]) clone.toArray(new SubsystemType[clone.size()]);
		}
		return null;
	}

	
	/**
	 * Create a subsystem controller for the given server type, system, and subsystemimpl id
	 * 
	 * If the given subsystem id is not found, null will be returned instead.
	 * 
	 * @param server
	 * @param system
	 * @param subsystemId
	 * @return  The specific subsystem being requested, or null if not found
	 * @throws CoreException
	 */
	public ISubsystemController createControllerForSubsystem(IServer server, String system, String subsystemId) throws CoreException {
		return createControllerForSubsystem(server, server.getServerType().getId(), system, subsystemId, null);
	}
	
	/**
	 * 
	 * Create a subsystem controller for the given server type, system, and subsystemimpl id
	 * with an environment to be provided for the controller to use in resolving dependencies 
	 * or other data.  
	 * 
	 * If the given subsystem id is not found, null will be returned instead.
	 * 
	 * @param server
	 * @param serverType
	 * @param system
	 * @param subsystemId
	 * @param env
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController createControllerForSubsystem(IServer server, 
			String serverType, String system, String subsystemId,
			Map<String, Object> env) throws CoreException {
		ISubsystemController c = createSubsystemController(server, serverType, system, null, subsystemId, env);
		if( c.getSubsystemId().equals(subsystemId))
			return c;
		return null;
	}

	
	/**
	 * Create a subsystem controller for the given server type and system
	 * @param server
	 * @param system
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController createSubsystemController(IServer server, String system) throws CoreException {
		return createSubsystemController(server, system, (Map<String,String>)null, null, null);
	}
	
	/**
	 * 
	 * Create a subsystem controller for the given server type and system, and use the provided environment
	 * @param server
	 * @param system
	 * @param env
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController createSubsystemController(IServer server, String system, Map<String, Object> env) throws CoreException {
		return createSubsystemController(server, system, (Map<String,String>)null, null, env);
	}

	
	/**
	 * Create a subsystem controller for the given server type and system. A map of required
	 * properties that the system must have declared on it may also be provided, as well
	 * as a default subsystem implementation to use in the event there are multiple matches.
	 * 
	 * @param server
	 * @param system
	 * @param requiredProperties
	 * @param defaultSubsystem
	 * @param environment An environment to be passed in
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController createSubsystemController(IServer server, String system, 
			Map<String, String> requiredProperties, String defaultSubsystem, 
			Map<String, Object> environment) throws CoreException {
		return createSubsystemController(server, server.getServerType().getId(), system, requiredProperties, defaultSubsystem, environment);
	}
	
	
	/**
	 * This signature is mostly unnecessary but is exposed for testing. 
	 * Please use the other shorter signature, specifically those that do not
	 * require the server type.
	 * 
	 *  This method will traverse the model from the extension points, 
	 *  and find (and then instantiate a new instance of) a controller 
	 *  that matches the given criteria. It will also prime the controller
	 *  with a reference to the server, and its internal system type object.
	 *  
	 *  You should provide a server if your subsystem, or any of its dependent subsystems, 
	 *  require any information from the server.  You must provide a system 
	 *  that you are looking for (such as "publish" or "modules", and you may
	 *  provide a map of required properties the system must have. 
	 *  
	 *  You may also list a default system id, such as "publish.carrierPidgeon" to 
	 *  ensure in the case there are multiple possible "publish" systems that are valid, 
	 *  "publish.carrierPidgeon" is the one selected. 
	 *  
	 *  In the event a default is not provided, requiredProperties are not provided, or
	 *  any other reason there are multiple results, a flag "default" will be checked for the 
	 *  value "true" on the subsystem's extension point data. 
	 *  
	 *  In the event there are *still* multiple matches, the first will be chosen. 
	 * 
	 * @param server
	 * @param serverType
	 * @param system
	 * @param requiredProperties
	 * @param defaultSubsystem
	 * @param environment An environment to be passed in
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController createSubsystemController(IServer server, String serverType, String system, 
			Map<String, String> requiredProperties, String defaultSubsystem, 
			Map<String, Object> environment) throws CoreException {
		SubsystemType[] types = getSubsystemTypes(serverType, system);
		if( types == null )
			return null;
		
		types = removeInvalidTypes(types);
		
		if( requiredProperties != null )
			types = findTypesWithProperties(types, requiredProperties);
		else if( environment != null ) {
			Map<String, String> requiredProps = getRequiredPropsFromEnv(system, environment);
			types = findTypesWithProperties(types, requiredProps);
		}
		
		
		SubsystemType selected = null;
		if( defaultSubsystem != null ) {
			// See if we can find the one the one they want as default
			if( types.length > 0 ) {
				SubsystemType tmp = findSubsystemWithId(types, defaultSubsystem); 
				selected = tmp == null ? types[0] : tmp;
			} 		
		}
		
		if( selected == null ) {
			// The one they wanted as default is not found, or they did not set a default
			// Check for a default property on the types
			SubsystemType[] defaultsSet = findTypesWithProperty(types, "default", "true");
			
			// There may be multiple marked default (which would be a bug), use the first one
			if( defaultsSet.length > 0 ) {
				selected = defaultsSet[0];
				// TODO log warning?
			} else {
				// None marked default, use the first acceptable
				if( types.length > 0 )
					selected = types[0];
			}
		} 
		
		
		
		String msg = "No subsystem found for servertype " + serverType + " and system type " + system;
		if( selected == null ) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, msg));
		}
		
		try {
			ISubsystemController c = selected.createController();
			c.initialize(server, selected, environment);
			return c;
		} catch(CoreException ce) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, msg, ce));
		}
	}
	
	private Map<String, String> getRequiredPropsFromEnv(String system, Map<String, Object> environment) {
		String key = system + AbstractSubsystemController.REQUIRED_PROPERTIES_ENV_KEY;
		HashMap<String, String> requiredProps = null;
		Object val1 = environment.get(key);
		if( val1 != null && val1 instanceof String) {
			String val = (String)val1;
			requiredProps = new HashMap<String, String>();
			String[] perReq = val.split(";");
			for( int i = 0; i < perReq.length; i++ ) {
				String[] tmp = perReq[i].split("=");
				requiredProps.put(tmp[0], tmp[1]);
			}
		}
		return requiredProps;
	}
	
	private SubsystemType findSubsystemWithId(SubsystemType[] types, String id) {
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].id.equals(id))
				return types[i];
		}
		return null;
	}
	
	private SubsystemType[] findTypesWithProperty(SubsystemType[] types, String key, String val) {
		if( key == null || val == null )
			return types;
		Map<String, String> m = new HashMap<String, String>();
		m.put(key, val);
		return findTypesWithProperties(types, m);
	}

	private SubsystemType[] removeInvalidTypes(SubsystemType[] types) {
		ArrayList<SubsystemType> matching = new ArrayList<SubsystemType>();
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].isValid) 
				matching.add(types[i]);
		}
		return (SubsystemType[]) matching.toArray(new SubsystemType[matching.size()]);
	}
	
	private SubsystemType[] findTypesWithProperties(SubsystemType[] types, Map<String, String> requiredProps) {
		if( requiredProps == null )
			return types;
		if( requiredProps.size() == 0)
			return types;
		
		ArrayList<SubsystemType> matching = new ArrayList<SubsystemType>();
		for( int i = 0; i < types.length; i++ ) {
			boolean matchesAll = true;
			Iterator<String> j = requiredProps.keySet().iterator();
			while(j.hasNext()) {
				String k = j.next();
				String v = requiredProps.get(k);
				String typeVal = types[i].props.get(k);
				if( !v.equals(typeVal))
					matchesAll = false;
			}
			if( matchesAll ) 
				matching.add(types[i]);
		}
		return (SubsystemType[]) matching.toArray(new SubsystemType[matching.size()]);
	}
	
	private void checkLoaded() {
		if( model == null ) {
			loadModel();
		}
	}
	
	
	private synchronized void loadModel() {
		ArrayList<IStatus> warningsErrors = new ArrayList<IStatus>();
		
		// First create the model
		model = new HashMap<String, Map<String, SubsystemType>>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ASWTPToolsPlugin.PLUGIN_ID, "serverSubsystem"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			SubsystemType type = new SubsystemType(cf[i]);
			if( allAttributesPresent(type)) {
				for( int k = 0; k < type.serverTypes.length; k++ ) {
					Map<String, SubsystemType> map = model.get(type.serverTypes[k]);
					if( map == null ) {
						map = new HashMap<String, SubsystemType>();
						model.put(type.serverTypes[k], map);
					}
					if( map.containsKey(type.id)){
						// ERROR, two types added at the same key for the same server
						warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
								"Invalid - 2 controllers for same server,system,subsystem combination")); // TODO clean message
					} else {
						map.put(type.id, type);
					}
				}
			} else {
				// TODO log not adding this one because it is invalid
				warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Invalid controller: - missing attributes" + type.id));
			}
		}
		
		// Now validate it
		IStatus[] extra = validateModel();
		warningsErrors.addAll(Arrays.asList(extra));
		MultiStatus ms = new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, 0, 
				(IStatus[]) warningsErrors.toArray(new IStatus[warningsErrors.size()]),
				"Errors validating Server Subsystem Model", null);
		if( !ms.isOK() ) {
			ASWTPToolsPlugin.log(ms);
		}
	}
	
	private IStatus[] validateModel() {
		ArrayList<IStatus> warningsErrors = new ArrayList<IStatus>();
		Iterator<String> i = model.keySet().iterator();
		while(i.hasNext()) {
			String serverType = i.next();
			Map<String, SubsystemType> val = model.get(serverType);
			Collection<SubsystemType> types = val.values();
			Iterator<SubsystemType> it = types.iterator();
			while(it.hasNext()) {
				SubsystemType sub = it.next();
				IStatus s = validateSubsystem(serverType, sub);
				if( !s.isOK()) {
					sub.isValid = false;
					sub.validationError = s;
					warningsErrors.add(s);
				}
			}
		}
		return (IStatus[]) warningsErrors.toArray(new IStatus[warningsErrors.size()]);
	}
	
	// Has potential to cause infinite recursion (?)
	private IStatus validateSubsystem(String serverType, SubsystemType subsys) {
		ArrayList<IStatus> warningsErrors = new ArrayList<IStatus>();
		String[] requiredSystem = subsys.requiredSystems;
		for( int i = 0; i < requiredSystem.length; i++ ) {
			SubsystemType[] matched = getSubsystemTypes(serverType, requiredSystem[i]);
			// ensure all dependent subsystems have been validated first
			for( int k = 0; k < matched.length; k++ ) {
				validateSubsystem(serverType, matched[k]);
			}
			if( matched.length ==  0 || allInvalid(matched)) {
				warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Invalid controller: - missing required dependent system ")); // TODO clean this
			}
		}

		Map<String, String> m = subsys.requiredSubsystems;
		Iterator<String> j = m.keySet().iterator();
		while(j.hasNext()) {
			String system2 = j.next();
			String subsys2 = m.get(system2); 
			
			SubsystemType[] matched = getSubsystemTypes(serverType, system2);
			boolean found = false;
			SubsystemType found2 = null;
			for( int k = 0; k < matched.length && !found; k++ ) {
				if( matched[k].getId().equals(subsys2)) {
					found = true;
					found2 = matched[k];
				}
			}
			if( !found ) {
				warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Invalid controller: - missing required dependent subsystem ")); // TODO clean this
			} else {
				validateSubsystem(serverType, found2);
				if( !found2.isValid ) {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
							"Invalid controller: - required subsystem has errors: " + found2.validationError)); // TODO clean this
				}
			}
		}
		IStatus[] allErrors = (IStatus[]) warningsErrors.toArray(new IStatus[warningsErrors.size()]);
		return new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, 0, allErrors, "Errors validating serverType=" + serverType + " and subsystem " + subsys.id, null);
	}
	
	private boolean allInvalid(SubsystemType[] matched) {
		for( int i = 0; i < matched.length; i++ ) 
			if( matched[i].isValid)
				return false;
		return true;
	}
	
	private boolean allAttributesPresent(SubsystemType type) {
		return type.element != null && type.id != null && type.serverTypes != null && type.system != null;
	}	
	
}
