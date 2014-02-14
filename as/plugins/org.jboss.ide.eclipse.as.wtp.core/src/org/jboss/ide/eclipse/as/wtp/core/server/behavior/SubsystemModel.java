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
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

/**
 * @since 3.0
 */
public class SubsystemModel {
	public static final String REQUIRED_PROPERTIES_ENV_KEY = ".RESERVED_requiredProperties";
	
	
	private static SubsystemModel instance;
	public static SubsystemModel getInstance() {
		if( instance == null )
			instance = new SubsystemModel();
		return instance;
	}
		
	/**
	 * A map of the following structure:
	 *    serverTypeId -> HashMap<mappedId, SubsystemMapping >
	 */
	private Map<String, Map<String, SubsystemMapping>> mappedModel;
	
	/**
	 * A map of subsystems, from id to their instance
	 */
	private Map<String, SubsystemType> subsystemModel;

	
	public static class Subsystem {
		private SubsystemMapping mapping;
		public Subsystem(SubsystemMapping mapping) {
			this.mapping = mapping;
		}
		public String getSubsystemId() {
			return mapping.getSubsystem().getId();
		}
		public String getSystemId() {
			return mapping.getSubsystem().getSystem();
		}
		public String getMappedId() {
			return mapping.getMappedId();
		}
		public Map<String, String> getRequiredSubsystems() {
			return mapping.getSubsystem().getRequiredSubsystems();
		}
		public boolean isValid() {
			return mapping.isValid();
		}
		public IStatus getValidationError() {
			return mapping.getValidationError();
		}
	}
	
	/**
	 * Exposed for testing only
	 */
	public class SubsystemMapping {
		private boolean isValid = true;
		private IStatus validationError;
		private String serverTypeId;
		private String mappedId;
		private String subsystemId;
		
		public SubsystemMapping(String server, String mappedId, String subsystemId) {
			this.serverTypeId = server;
			this.mappedId = mappedId;
			this.subsystemId = subsystemId;
		}
		
		public boolean isValid() {
			return isValid;
		}
		public String getServerType() {
			return serverTypeId;
		}
		
		public IStatus getValidationError() {
			return validationError;
		}
		public String getSubsystemId() {
			return subsystemId;
		}
		public String getMappedId() {
			return mappedId;
		}
		public SubsystemType getSubsystem() {
			return subsystemModel.get(subsystemId);
		}
	}
	
	
	/**
	 * The subsystem type, exposed for testing only
	 */
	protected static class SubsystemType {
		// Are all dependencies available
		private String system;
		private String id;
		private String name;
		private boolean requiresRuntime;
		private IConfigurationElement element;
		private String[] requiredSystems;
		private HashMap<String, String> requiredSubsystems;
		private HashMap<String, String> props;
		
		public SubsystemType(IConfigurationElement element) {
			this.element = element;
			name = element.getAttribute("name");
			id = element.getAttribute("id");
			system = element.getAttribute("system");
			String reqRuntime = element.getAttribute("requiresRuntime");
			requiresRuntime = reqRuntime == null ? false : new Boolean(reqRuntime).booleanValue();
			
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
		public boolean requiresRuntime() {
			return requiresRuntime;
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
		
		public String toString() {
			return getSystem() + " - " + getId();
		}
	}

	/**
	 * Return only subsystem types for this server for the appropriate system
	 * @param serverTypeId
	 * @param system
	 * @return
	 */
	protected SubsystemMapping[] getAllMappings(String serverType) {
		checkLoaded();
		Map<String, SubsystemMapping> map = mappedModel.get(serverType);
		if( map != null ) {
			Collection<SubsystemMapping> vals = map.values();
			return (SubsystemMapping[]) vals.toArray(new SubsystemMapping[vals.size()]);
		}
		return new SubsystemMapping[0];
	}
	
	protected String[] getAllSystemsForServertype(String serverType) {
		ArrayList<String> ret = new ArrayList<String>();
		SubsystemMapping[] all = getAllMappings(serverType);
		String sys = null;
		for( int i = 0; i < all.length; i++ ) {
			sys = all[i].getSubsystem().getSystem();
			if( !ret.contains(sys))
				ret.add(sys);
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * Return only subsystem types for this server for the appropriate system
	 * @param serverTypeId
	 * @param system
	 * @return
	 */
	protected SubsystemMapping[] getSubsystemMappings(String serverType, String system) {
		checkLoaded();
		Map<String, SubsystemMapping> serverSubsystems = mappedModel.get(serverType);
		if( serverSubsystems != null ) {
			Collection<SubsystemMapping> mappings = serverSubsystems.values();
			if( system == null ) {
				return mappings.toArray(new SubsystemMapping[mappings.size()]);
			}
			// otherwise get rid of any that dont match category
			ArrayList<SubsystemMapping> clone = new ArrayList<SubsystemMapping>(mappings);
			Iterator<SubsystemMapping> i = clone.iterator();
			SubsystemMapping tmp = null;
			while(i.hasNext()) {
				tmp = i.next();
				if( !tmp.getSubsystem().getSystem().equals(system)) {
					i.remove();
				}
			}
			return (SubsystemMapping[]) clone.toArray(new SubsystemMapping[clone.size()]);
		}
		return new SubsystemMapping[0];
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
		if( c.getSubsystemMappedId().equals(subsystemId))
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
	public ISubsystemController createSubsystemController(IServerAttributes server, String system, Map<String, Object> env) throws CoreException {
		return createSubsystemController(server, system, (Map<String, String>)null, null, env);
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
	public ISubsystemController createSubsystemController(IServerAttributes server, String system, 
			Map<String, String> requiredProperties, String defaultSubsystem, 
			Map<String, Object> environment) throws CoreException {
		return createSubsystemController(server, server.getServerType().getId(), system, requiredProperties, defaultSubsystem, environment);
	}
	
	public SubsystemMapping getSubsystemMappingForCreation(String serverType, String system, 
			Map<String, String> requiredProperties, String defaultSubsystem, 
			Map<String, Object> environment) throws CoreException {	
		checkLoaded();
		SubsystemMapping[] types = getSubsystemMappings(serverType, system);
		if( types == null )
			types = new SubsystemMapping[0];
		
		types = removeInvalidTypes(types);
		
		if( requiredProperties != null )
			types = findTypesWithProperties(types, requiredProperties);
		else if( environment != null ) {
			Map<String, String> requiredProps = getRequiredPropsFromEnv(system, environment);
			types = findTypesWithProperties(types, requiredProps);
		}
		
		
		SubsystemMapping selectedMapping = null;
		if( defaultSubsystem != null ) {
			// See if we can find the one they want as default
			if( types.length > 0 ) {
				SubsystemMapping tmp = findSubsystemWithMappedId(types, defaultSubsystem); 
				selectedMapping = tmp == null ? types[0] : tmp;
			} 		
		}
		
		if( types.length == 1 ) {
			selectedMapping = types[0];
		}
		
		if( selectedMapping == null ) {
			// The one they wanted as default is not found, or they did not set a default
			// Check for a default property on the types
			SubsystemMapping[] defaultsSet = findMappingsWithProperty(types, "default", "true");
			
			// There may be multiple marked default (which would be a bug), use the first one
			if( defaultsSet.length > 0 ) {
				selectedMapping = defaultsSet[0];
				// TODO log warning?
			} else {
				// None marked default, use the first acceptable
				if( types.length > 1 ) {
					String msg = "Unable to choose a subsystem. Multiple subsystems found for servertype " + serverType + " and system type " + system;
					throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, msg));
				}
			}
		} 
		
		
		
		if( selectedMapping == null ) {
			String msg = "No subsystem found for servertype " + serverType + " and system type " + system;
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, msg));
		}
		return selectedMapping;
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
	public ISubsystemController createSubsystemController(IServerAttributes server, String serverType, String system, 
		Map<String, String> requiredProperties, String defaultSubsystem, 
		Map<String, Object> environment) throws CoreException {

		SubsystemMapping selectedMapping = getSubsystemMappingForCreation(serverType, system, requiredProperties, defaultSubsystem, environment);
		try {
			ISubsystemController c = selectedMapping.getSubsystem().createController();
			c.initialize(server, new Subsystem(selectedMapping), environment);
			return c;
		} catch(CoreException ce) {
			String msg = "Error creating subsystem " + selectedMapping.mappedId + " for server type " + serverType;
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, msg, ce));
		}
	}
	
	private Map<String, String> getRequiredPropsFromEnv(String system, Map<String, Object> environment) {
		String key = system + REQUIRED_PROPERTIES_ENV_KEY;
		HashMap<String, String> requiredProps = null;
		if( environment == null )
			return new HashMap<String, String>();
		
		Object val1 = environment.get(key);
		if( val1 instanceof String) {
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
	
	private SubsystemMapping findSubsystemWithMappedId(SubsystemMapping[] types, String id) {
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].mappedId.equals(id))
				return types[i];
		}
		return null;
	}
	
	private SubsystemMapping[] findMappingsWithProperty(SubsystemMapping[] types, String key, String val) {
		if( key == null || val == null )
			return types;
		Map<String, String> m = new HashMap<String, String>();
		m.put(key, val);
		return findTypesWithProperties(types, m);
	}

	private SubsystemMapping[] removeInvalidTypes(SubsystemMapping[] types) {
		ArrayList<SubsystemMapping> matching = new ArrayList<SubsystemMapping>();
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].isValid) 
				matching.add(types[i]);
		}
		return (SubsystemMapping[]) matching.toArray(new SubsystemMapping[matching.size()]);
	}
	
	private SubsystemMapping[] findTypesWithProperties(SubsystemMapping[] types, Map<String, String> requiredProps) {
		if( requiredProps == null || requiredProps.size() == 0)
			return types;
		
		ArrayList<SubsystemMapping> matching = new ArrayList<SubsystemMapping>();
		for( int i = 0; i < types.length; i++ ) {
			boolean matchesAll = true;
			Iterator<String> j = requiredProps.keySet().iterator();
			while(j.hasNext() && matchesAll) {
				String k = j.next();
				String v = requiredProps.get(k);
				String typeVal = types[i].getSubsystem().props.get(k);
				if( !v.equals(typeVal))
					matchesAll = false;
			}
			if( matchesAll ) 
				matching.add(types[i]);
		}
		return matching.toArray(new SubsystemMapping[matching.size()]);
	}
	
	private void checkLoaded() {
		if( subsystemModel == null ) {
			loadModel();
		}
	}
	
	
	private synchronized void loadModel() {
		ArrayList<IStatus> warningsErrors = new ArrayList<IStatus>();
		
		// First create the models
		subsystemModel = new HashMap<String, SubsystemType>();
		mappedModel = new HashMap<String, Map<String,SubsystemMapping>>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ASWTPToolsPlugin.PLUGIN_ID, "serverSubsystem"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			String name = cf[i].getName();
			if( "subsystem".equals(name)) {
				SubsystemType type = new SubsystemType(cf[i]);
				SubsystemType existing = subsystemModel.get(type.id);
				if( existing != null ) {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
					"Invalid - 2 subsystems have been declared with the same unique id: " + type.id));
				} else if( hasRequiredSubsystemAttributes(type)){
					subsystemModel.put(type.id, type);
				} else {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
					"Invalid - Subsystem missing required attributes: " + type.id));
				}
			} else if ( "subsystemMapping".equals(name)) {
				String subsystemId = cf[i].getAttribute("id");
				String mappedId = cf[i].getAttribute("mappedId");
				String serverTypes = cf[i].getAttribute("serverTypes");
				if( subsystemId != null && mappedId != null && serverTypes != null) {
					String[] split = serverTypes.split(",");
					for( int j = 0; j < split.length; j++ ) {
						Map m = mappedModel.get(split[j]);
						if( m == null ) {
							m = new HashMap<String, String>();
							mappedModel.put(split[j], m);
						}
						mappedModel.get(split[j]).put(mappedId, new SubsystemMapping(split[j], mappedId, subsystemId));
					}
				} else {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
					"Invalid - Subsystem Mapping missing required attributes: " + subsystemId + ", " + mappedId + ", " + serverTypes));
				}
			}
		}
		
		// Now validate the entire model after all mappings and systems have been loaded
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
		for (Map.Entry<String, Map<String, SubsystemMapping>> entry : mappedModel.entrySet()) {
		    String serverType = entry.getKey();
		    Map<String, SubsystemMapping> val = entry.getValue();
			Collection<SubsystemMapping> types = val.values();
			Iterator<SubsystemMapping> it = types.iterator();
			while(it.hasNext()) {
				SubsystemMapping mapping = it.next();
				SubsystemType type = mapping.getSubsystem();
				if( type != null ) {
					IStatus s = validateSubsystemMapping(mapping);
					if( !s.isOK()) {
						mapping.isValid = false;
						mapping.validationError = s;
						warningsErrors.add(s);
					}

				} else {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
					"Invalid - Mapped Subsystem does not exist: " + serverType + ", " + type.id));
				}
			}
		}
		return (IStatus[]) warningsErrors.toArray(new IStatus[warningsErrors.size()]);
	}
	
	// Has potential to cause infinite recursion (?)
	private IStatus validateSubsystemMapping(SubsystemMapping mapping) {
		ArrayList<IStatus> warningsErrors = new ArrayList<IStatus>();
		String serverType = mapping.getServerType();
		
		// First we validate requirements on generic systems
		String[] requiredSystem = mapping.getSubsystem().getRequiredSystems();
		for( int i = 0; i < requiredSystem.length; i++ ) {
			SubsystemMapping[] mappingsForSystem = getSubsystemMappings(serverType, requiredSystem[i]);
			// ensure all dependent subsystems have been validated first
			for( int k = 0; k < mappingsForSystem.length; k++ ) {
				validateSubsystemMapping(mappingsForSystem[k]);
			}
			if( mappingsForSystem.length ==  0 || allInvalid(mappingsForSystem)) {
				warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Invalid controller: - missing required dependent system " + requiredSystem[i])); // TODO clean this
			}
		}
		
		// Now we validate requirements on specific subsystem mapped id's.  
		Map<String, String> m = mapping.getSubsystem().getRequiredSubsystems();
		for (Map.Entry<String, String> entry : m.entrySet()) {
		    String system2 = entry.getKey();
		    Object subsys2 = entry.getValue();
			SubsystemMapping[] matched = getSubsystemMappings(serverType, system2);
			boolean found = false;
			SubsystemMapping found2 = null;
			for( int k = 0; k < matched.length && !found; k++ ) {
				if( matched[k].mappedId.equals(subsys2)) {
					found = true;
					found2 = matched[k];
				}
			}
			if( !found ) {
				warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
						"Invalid controller: - missing required dependent subsystem ")); // TODO clean this
			} else {
				validateSubsystemMapping(found2);
				if( !found2.isValid ) {
					warningsErrors.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
							"Invalid controller: - required subsystem has errors: " + found2.validationError)); // TODO clean this
				}
			}
		}
		IStatus[] allErrors = (IStatus[]) warningsErrors.toArray(new IStatus[warningsErrors.size()]);
		return new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, 0, allErrors, "Errors validating subsystem mapping=" + serverType + " and subsystem id " + mapping.getSubsystem().getId(), null);
	}
	
	private boolean allInvalid(SubsystemMapping[] matched) {
		for( int i = 0; i < matched.length; i++ ) 
			if( matched[i].isValid)
				return false;
		return true;
	}
	
	private boolean hasRequiredSubsystemAttributes(SubsystemType type) {
		return type.element != null && type.id != null && type.system != null;
	}	
	
}
