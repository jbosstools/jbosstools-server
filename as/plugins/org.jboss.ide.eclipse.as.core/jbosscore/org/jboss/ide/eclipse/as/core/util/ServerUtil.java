/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;

public class ServerUtil {
	public static IPath getServerStateLocation(IServer server) {
		return server == null ? JBossServerCorePlugin.getDefault().getStateLocation() :
					getServerStateLocation(server.getId());
	}

	public static IPath getServerStateLocation(String serverID) {
		return serverID == null ? JBossServerCorePlugin.getDefault().getStateLocation() : 
			JBossServerCorePlugin.getDefault().getStateLocation()
			.append(serverID.replace(' ', '_'));
	}

	public static IPath getServerBinDirectory(JBossServer server) throws CoreException {
		return getServerHomePath(server).append(IJBossRuntimeResourceConstants.BIN);
	}
	
	@SuppressWarnings("unchecked")
	public static <ADAPTER> ADAPTER checkedGetServerAdapter(IServerAttributes server, Class<ADAPTER> behaviorClass) throws CoreException {
		ADAPTER adapter = (ADAPTER) server.loadAdapter(behaviorClass, new NullProgressMonitor());
		if (adapter == null) {
			throw new CoreException(					
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.CouldNotFindServerBehavior, server.getName())));
		}
		return adapter;
	}
	
	public static IJBossBehaviourDelegate checkedGetBehaviorDelegate(IServer server) throws CoreException {
		return checkedGetServerAdapter(server, IDelegatingServerBehavior.class).getDelegate();
	}

	public static IPath makeRelative(IRuntime rt, IPath p) {
		if( p.isAbsolute() && rt != null) {
			if(rt.getLocation().isPrefixOf(p)) {
				int size = rt.getLocation().toOSString().length();
				return new Path(p.toOSString().substring(size)).makeRelative();
			}
		}
		return p;
	}
	
	public static IPath makeGlobal(IRuntime rt, IPath p) {
		return rt == null ? p : makeGlobal(rt.getLocation(), p);
	}
	
	public static IPath makeGlobal(IPath rtLocation, IPath p) {
		if( !p.isAbsolute() ) {
			if( rtLocation != null && rtLocation != null ) {
				return rtLocation.append(p).makeAbsolute();
			}
			return p.makeAbsolute();
		}
		return p;
	}
	
	public static boolean isJBossServerType(IServerType type) {
		if( type == null )
			return false;
		
		// If we start with AS or EAP serverIds and are NOT deploy-only server
		return !type.getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER) &&
				(type.getId().startsWith(IJBossToolingConstants.SERVER_AS_PREFIX) 
						|| type.getId().startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX));
	}
	
	
	/**
	 * This method should not be used because it is potentially misleading. 
	 * 
	 * This method *now* returns whether a server is in general jboss7 style server. 
	 * However this is very vague. Small things change from release to release, and 
	 * being of a general style does not mean all behavior is identical. 
	 * 
	 * Users should find some other way to determine the generic structure and 
	 * behavior of a server. Many possibilities are in ServerExtendedProperties 
	 * and all of their subclasses. 
	 *  
	 *  The currently implementation will now group wildfly in with the as7-style servers. 
	 *  All clients should quickly move to find specific properties they need access too, 
	 *  and should not use a method as vague as this. 
	 * 
	 * @deprecated

	 * @param server
	 * @return
	 */
	public static boolean isJBoss7(IServer server) {
		return isJBoss7Style(server);
	}
	
	/**
	 * This method should not be used because it is potentially misleading. 
	 * 
	 * This method *now* returns whether a server is in general jboss7 style server. 
	 * However this is very vague. Small things change from release to release, and 
	 * being of a general style does not mean all behavior is identical. 
	 * 
	 * Users should find some other way to determine the generic structure and 
	 * behavior of a server. Many possibilities are in ServerExtendedProperties 
	 * and all of their subclasses. 
	 *  
	 *  The currently implementation will now group wildfly in with the as7-style servers. 
	 *  All clients should quickly move to find specific properties they need access too, 
	 *  and should not use a method as vague as this. 
	 * 
	 * @deprecated
	 * @param type
	 * @return
	 */
	public static boolean isJBoss7(IServerType type) {
		if( type == null )
			return false;
		return type.getId().equals(IJBossToolingConstants.SERVER_AS_70)
				 || type.getId().equals(IJBossToolingConstants.SERVER_AS_71)
				 || type.getId().equals(IJBossToolingConstants.SERVER_EAP_60)
				 || type.getId().equals(IJBossToolingConstants.SERVER_EAP_61)
				 || type.getId().equals(IJBossToolingConstants.SERVER_WILDFLY_80);
	}
	
	private static boolean isJBoss7Style(IServer server) {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		boolean as7Style = sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS; 
		return as7Style;
	}
	
	public static void createStandardFolders(IServer server) {
		// create metadata area
		File location = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		location.mkdirs();
		File d1 = new File(location, IJBossRuntimeResourceConstants.DEPLOY);
		File d2 = new File(location, IJBossToolingConstants.TEMP_DEPLOY);
		d1.mkdirs();
		d2.mkdirs();
		
		
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		int fileStructure = sep.getFileStructure();
		// create temp deploy folder
		JBossServer ds = ( JBossServer)server.loadAdapter(JBossServer.class, null);
		if( ds != null && fileStructure == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY) {
			// Only add these folders for as < 7
			if( !new File(ds.getDeployFolder()).equals(d1)) 
				new File(ds.getDeployFolder()).mkdirs();
			if( !new File(ds.getTempDeployFolder()).equals(d2))
				new File(ds.getTempDeployFolder()).mkdirs();
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			if( jbsrt != null ) {
				String config = jbsrt.getJBossConfiguration();
				IPath newTemp = new Path(IJBossRuntimeResourceConstants.SERVER).append(config)
					.append(IJBossToolingConstants.TMP)
					.append(IJBossToolingConstants.JBOSSTOOLS_TMP).makeRelative();
				IPath newTempAsGlobal = makeGlobal(jbsrt.getRuntime(), newTemp);
				newTempAsGlobal.toFile().mkdirs();
			}
		}
	}
	
	public static IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if (name.trim().equals(servers[i].getName()))
				return servers[i];
		}
		return null;
	}
	
	public static String getDefaultServerName(IRuntime rt) {
		String runtimeName = rt.getName();
		String base = null;
		if( runtimeName == null || runtimeName.equals("")) //$NON-NLS-1$
			base = NLS.bind(Messages.serverVersionName, rt.getRuntimeType().getVersion());
		else 
			base = NLS.bind(Messages.serverName, runtimeName);
		
		return getDefaultServerName( base);
	}
	public static String getDefaultServerName( String base) {
		if( ServerUtil.findServer(base) == null ) return base;
		int i = 1;
		while( ServerUtil.findServer(
				NLS.bind(Messages.serverCountName, base, i)) != null )
			i++;
		return NLS.bind(Messages.serverCountName, base, i);
	}
	
	public static String checkedGetServerHome(JBossServer jbs) throws CoreException {
		String serverHome = jbs.getServer().getRuntime().getLocation().toOSString();
		if (serverHome == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.CannotLocateServerHome, jbs.getServer().getName())));
		return serverHome;
	}
	
	public static IPath getServerHomePath(JBossServer jbs) throws CoreException {
		return new Path(checkedGetServerHome(jbs));
	}
	
	
    /**
	 * @since 2.3
	 */
    public static String getFromSecureStorage(IServerAttributes server, String key) {
        try {
        	ISecurePreferences node = getNode(server);
            String val = node.get(key, null);
            if (val == null) {
            	return null;
            }
            return new String(EncodingUtils.decodeBase64(val));
        } catch(IOException e) {
        	return null;
        } catch (StorageException e) {
        	return null;
		}
    }
    
	private static final String PREFERNCES_BASEKEY = JBossServerCorePlugin.PLUGIN_ID.replace('.', Path.SEPARATOR);

    /**
	 * @since 2.3
	 */
    public static void storeInSecureStorage(IServerAttributes server, String key, String val ) throws StorageException, UnsupportedEncodingException {
        ISecurePreferences node = getNode(server);
        if( val == null )
        	node.put(key, val, true);
        else
        	node.put(key, EncodingUtils.encodeBase64(val.getBytes()), true /* encrypt */); 
    }

    private static ISecurePreferences getNode(IServerAttributes server) throws UnsupportedEncodingException {
		String secureKey = new StringBuilder(PREFERNCES_BASEKEY)
			.append(server.getName())
			.append(Path.SEPARATOR).toString();

		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		String encoded = URLEncoder.encode(secureKey, "UTF-8"); //$NON-NLS-1$
		return root.node(encoded);
    }
    public static String formatPossibleIpv6Address(String address) {
            if (address == null) {
                return address;
            }
            if (!address.contains(":")) { //$NON-NLS-1$
                return address;
            }
            if (address.startsWith("[") && address.endsWith("]")) { //$NON-NLS-1$  //$NON-NLS-2$
                return address;
            }
            return "[" + address + "]"; //$NON-NLS-1$//$NON-NLS-2$
        }
}
