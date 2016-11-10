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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
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
import org.jboss.ide.eclipse.as.wtp.core.util.ServerSecureStorageUtil;

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
			IPath rtLoc = rt.getLocation();
			if(rtLoc != null && rtLoc.isPrefixOf(p)) {
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
		List<String> asList = Arrays.asList(IJBossToolingConstants.ALL_JBOSS_SERVERS);
		return asList.contains(type.getId());
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
				 || type.getId().equals(IJBossToolingConstants.SERVER_WILDFLY_80)
				 || type.getId().equals(IJBossToolingConstants.SERVER_WILDFLY_90)
				 || type.getId().equals(IJBossToolingConstants.SERVER_WILDFLY_100)
				 || type.getId().equals(IJBossToolingConstants.SERVER_EAP_60)
				 || type.getId().equals(IJBossToolingConstants.SERVER_EAP_61)
				 || type.getId().equals(IJBossToolingConstants.SERVER_EAP_70);
	}
	
	private static boolean isJBoss7Style(IServer server) {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if( sep == null )
			return false;
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
		
		
		JBossServer ds = ( JBossServer)server.loadAdapter(JBossServer.class, null);
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		if( ds != null && sep != null ) {
			int fileStructure = sep.getFileStructure();
			// create temp deploy folder
			if( fileStructure == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY) {
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
		return ServerNamingUtility.getDefaultServerName(rt);
	}

	public static String getDefaultServerName( String base) {
		return ServerNamingUtility.getDefaultServerName(base);
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
	
	private static final String LEGACY_SECURE_PREFERNCES_BASEKEY = JBossServerCorePlugin.PLUGIN_ID.replace('.', Path.SEPARATOR);
	private static final String SECURE_PREFERNCES_BASEKEY = JBossServerCorePlugin.PLUGIN_ID;
	
    /**
	 * @since 2.3
	 */
    public static String getFromSecureStorage(IServerAttributes server, String key) {
    	String ret = ServerSecureStorageUtil.getFromSecureStorage(SECURE_PREFERNCES_BASEKEY, server, key);
    	if( ret == null ) {
    		ret = ServerSecureStorageUtil.legacyGetFromSecureStorage(LEGACY_SECURE_PREFERNCES_BASEKEY, server, key);
    	}
    	return ret;
    }
    
    /**
	 * @since 2.3
	 */
    public static void storeInSecureStorage(IServerAttributes server, String key, String val ) throws StorageException, UnsupportedEncodingException {
    	ServerSecureStorageUtil.storeInSecureStorage(SECURE_PREFERNCES_BASEKEY, server, key, val);
    }

    /**
     * Create a URL string which is safe for all valid versions of IP.
     *  
     * For example, given a host 192.168.1.1, scheme http, and path my/folder, 
     * this will return  http://192.168.1.1/my/folder 
     * 
     * @param scheme  A scheme to connect over
     * @param host    A host
     * @param path    A path
     * @return
     */
    public static String createSafeURLString(String scheme, String host, String path) {
    	return createSafeURLString(scheme, host, -1, path);
    }
    
    /**
     * Create a URL string which is safe for all valid versions of IP.
     *  
     * For example, given a host 5a:55:4f:e6:e7:ea, scheme http, port 8080, and path my/folder, 
     * this will return  http://[5a:55:4f:e6:e7:ea]:8080/my/folder 
     * 
     * @param scheme  A scheme to connect over
     * @param host    A host
     * @param port    A port to connect over, or -1 if none is to be set
     * @param path    A path
     * @return
     */
    public static String createSafeURLString(String scheme, String host, int port, String path) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(scheme);
    	sb.append("://"); //$NON-NLS-1$
    	sb.append(formatPossibleIpv6Address(host));
    	if( port != -1 ) {
    		sb.append(":"); //$NON-NLS-1$
    		sb.append(port);
    	}
    	if( path != null ) {
    		if( !path.startsWith("/")) //$NON-NLS-1$
    			sb.append("/"); //$NON-NLS-1$
    		sb.append(path);
    	}
    	return sb.toString();
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
    
    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static Pattern VALID_IPV6_COMPRESSED_PATTERN = null;
    
    private static final String ip4PatternString = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"; //$NON-NLS-1$
	private static final String ip6StandardPatternString = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"; //$NON-NLS-1$
	private static final String ip6CompressedPatternString = "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$"; //$NON-NLS-1$

    static {
      try {
        VALID_IPV4_PATTERN = Pattern.compile(ip4PatternString, Pattern.CASE_INSENSITIVE);
        VALID_IPV6_PATTERN = Pattern.compile(ip6StandardPatternString, Pattern.CASE_INSENSITIVE);
        VALID_IPV6_COMPRESSED_PATTERN = Pattern.compile(ip6CompressedPatternString, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException e) {
        //logger.severe("Unable to compile pattern", e);
      }
    }

	public static boolean matchesIP4t(String ipAddress) {
		Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
		return m1.matches();
	}

	public static boolean matchesIP6t(String ipAddress) {
		Matcher m1 = VALID_IPV6_PATTERN.matcher(ipAddress);
		if( m1.matches() )
			return true;
		Matcher m2 = VALID_IPV6_COMPRESSED_PATTERN.matcher(ipAddress);
		return m2.matches();
	}
    
}
