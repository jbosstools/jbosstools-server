/*******************************************************************************
 * Copyright (c) 2011-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.internal.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * @author Marshall
 * @author Rob Stryker
 */
public class EJB3ClasspathContainer
		implements IClasspathContainer, IJBossRuntimeConstants, IJBossRuntimeResourceConstants, IJBossToolingConstants {
	public static final String CONTAINER_ID = "org.jboss.ide.eclipse.as.classpath.core.ejb3.classpathContainer"; //$NON-NLS-1$

	public static final String DESCRIPTION = Messages.EJB3ClasspathContainer_ejb3_description;

	public static final QualifiedName JBOSS_EJB3_CONFIGURATION = new QualifiedName(
			"org.jboss.ide.eclipse.ejb3.wizards.core.classpath", "jboss-ejb3-configuration"); //$NON-NLS-1$ //$NON-NLS-2$

	protected IJavaProject javaProject;
	protected IJBossServer jbossServer;
	protected IPath path;

	protected IPath configPath = new Path(""); //$NON-NLS-1$
	protected IPath homePath = null;

	public EJB3ClasspathContainer(IPath path, IJavaProject project) {
		this.path = path;
		this.javaProject = project;
		String configName = path.segment(1);
		IProject p = project == null ? null : project.getProject();
		if (configName == null && p != null && p.exists() && p.isOpen())
			configName = findLegacyConfigName(project);
		IServer foundServer = findServer(configName);
		jbossServer = ServerConverter.getJBossServer(foundServer);
		if (jbossServer != null) {
			try {
				homePath = jbossServer.getServer().getRuntime().getLocation();
				configPath = new Path(jbossServer.getConfigDirectory());
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, ClasspathCorePlugin.PLUGIN_ID,
						Messages.EJB3ClasspathContainer_could_not_determine_home, e);
				ClasspathCorePlugin.getDefault().getLog().log(status);
			}
		}
	}

	private String findLegacyConfigName(IJavaProject project) {
		if (project == null)
			return null;
		String configName = null;
		try {
			// old classpath container, try finding the persisten property
			configName = project.getProject().getPersistentProperty(JBOSS_EJB3_CONFIGURATION);
			if (configName != null) {
				// go ahead and remove the persistent property
				project.getProject().setPersistentProperty(JBOSS_EJB3_CONFIGURATION, null);
			}
		} catch (CoreException ce) {
			// This should never occur. If it does, it regards legacy situations.
			// The project is opened and accessible. There are no reasons for this
			// to ever occur.
		}
		return configName;
	}

	private IServer findServer(String serverName) {
		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].getName().equals(serverName)) {
				return servers[i];
			}
		}
		return null;
	}

	public String getDescription() {
		return Messages.EJB3ClasspathContainer_ejb30_description;
	}

	public IJBossServer getJBossServer() {
		return jbossServer;
	}

	public void setJBossServer(IJBossServer jbossServer) {
		this.jbossServer = jbossServer;
	}

	public int getKind() {
		return K_APPLICATION;
	}

	public IPath getPath() {
		return path;
	}

	public IClasspathEntry[] getClasspathEntries() {
		IClasspathEntry[] ret = new IClasspathEntry[] {};
		if (jbossServer != null) {
			try {
				String id = jbossServer.getServer().getServerType().getRuntimeType().getId();
				switch (id) {
				case AS_40:
					ret = get40Jars(homePath, configPath);
					break;
				case AS_42:
				case EAP_43:
					ret = get42Jars(homePath, configPath);
					break;
				case AS_50:
					ret = get50Jars(homePath, configPath, true, true);
					break;
				case EAP_50:
					ret = get50Jars(homePath, configPath, false, false);
					break;
				case AS_51:
					ret = get51Jars(homePath, configPath);
					break;
				case AS_60:
					ret = get60Jars(homePath, configPath);
					break;
				case AS_70:
				case AS_71:
				case AS_72:
					ret = get70Jars(homePath);
					break;
				case EAP_60:
					ret = get70Jars(homePath);
					break;
				case WILDFLY_80:
				case WILDFLY_90:
				case WILDFLY_100:
				case WILDFLY_110:
				case WILDFLY_120:
				case WILDFLY_130:
				case WILDFLY_140:
                case WILDFLY_150:
                case WILDFLY_160:
                case EAP_61:
                case EAP_70:
                case WILDFLY_170:
                case WILDFLY_180:
                case EAP_71:
                case EAP_72:
                case EAP_73:
                case WILDFLY_190:
                case WILDFLY_200:
                case WILDFLY_210:
                case WILDFLY_220:
                case WILDFLY_230:
                case EAP_74:
                case WILDFLY_240:
                // AUTOGEN_SERVER_ADAPTER_CHUNK
					ret = getEap61Jars(homePath);
					break;
				default:
					break;
				}

				// NEW_SERVER_ADAPTER
			} catch (FileNotFoundException fnfe) {
			}
		}
		return ret;
	}

	public static IClasspathEntry[] get40Jars(IPath homePath, IPath configPath) throws FileNotFoundException {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();

		// path roots
		IPath deploy = configPath.append(DEPLOY);
		IPath deployer = deploy.append(EJB3_DEPLOYER);
		IPath aopDeployer = deploy.append(AOP_JDK5_DEPLOYER);

		// ejb3
		list.add(getEntry(deployer.append(JBOSS_EJB3X_JAR)));
		list.add(getEntry(deployer.append(JBOSS_EJB3_JAR)));
		list.add(getEntry(deployer.append(JBOSS_ANNOTATIONS_EJB3_JAR)));

		// aop
		list.add(getEntry(aopDeployer.append(JBOSS_AOP_JDK5_JAR)));
		list.add(getEntry(aopDeployer.append(JBOSS_ASPECT_LIBRARY_JDK5_0)));

		// hibernate
		list.add(getEntry(homePath.append(CLIENT).append(HIBERNATE_CLIENT_JAR)));
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	public static IClasspathEntry[] get42Jars(IPath homePath, IPath configPath) throws FileNotFoundException {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();

		// path roots
		IPath deploy = configPath.append(DEPLOY);
		IPath deployer = deploy.append(EJB3_DEPLOYER);
		IPath aopDeployer = deploy.append(AOP_JDK5_DEPLOYER);
		IPath client = homePath.append(CLIENT);

		list.add(getEntry(configPath.append(LIB).append(JBOSS_EJB3X_JAR)));
		list.add(getEntry(deployer.append(JBOSS_EJB3_JAR)));
		list.add(getEntry(deployer.append(JBOSS_ANNOTATIONS_EJB3_JAR)));

		// aop
		list.add(getEntry(aopDeployer.append(JBOSS_AOP_JDK5_JAR)));
		list.add(getEntry(aopDeployer.append(JBOSS_ASPECT_LIBRARY_JDK5_0)));

		// hibernate
		list.add(getEntry(homePath.append(CLIENT).append(HIBERNATE_CLIENT_JAR)));

		// persistence jar
		list.add(getEntry(client.append(EJB3_PERSISTENCE_JAR)));
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	protected static IClasspathEntry[] get50Jars(IPath homePath, IPath configPath, boolean includeEJB3Proxy,
			boolean includeIIOP) throws FileNotFoundException {
		IPath deployers = configPath.append(DEPLOYERS);
		IPath deployer = deployers.append(EJB3_DEPLOYER);
		IPath aopDeployer = deployers.append(AOP_JBOSS5_DEPLOYER);
		IPath client = homePath.append(CLIENT);
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.add(getEntry(aopDeployer.append(JBOSS5_ASPECT_LIBRARY_JAR)));
		list.add(getEntry(deployer.append(JB5_EJB_DEPLOYER_JAR)));
		if (includeIIOP)
			list.add(getEntry(deployer.append(JB5_EJB_IIOP_JAR)));
		list.add(getEntry(client.append(EJB3_PERSISTENCE_JAR)));
		list.add(getEntry(client.append(jboss_ejb3_common_client)));
		list.add(getEntry(client.append(jboss_ejb3_core_client)));
		list.add(getEntry(client.append(jboss_ejb3_ext_api_impl)));
		list.add(getEntry(client.append(jboss_ejb3_ext_api)));
		list.add(getEntry(client.append(jboss_ejb3_proxy_clustered_client)));
		list.add(getEntry(client.append(jboss_ejb3_security_client)));
		list.add(getEntry(homePath.append(CLIENT).append(JB50_HIBERNATE_ANNOTATIONS_JAR)));
		if (includeEJB3Proxy)
			list.add(getEntry(client.append(jboss_ejb3_proxy_client)));
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	protected static IClasspathEntry[] get51Jars(IPath homePath, IPath configPath) throws FileNotFoundException {
		return get50Jars(homePath, configPath, false, true);
	}

	protected static IClasspathEntry[] get60Jars(IPath homePath, IPath configPath) throws FileNotFoundException {
		IPath deployers = configPath.append(DEPLOYERS);
		IPath aopDeployer = deployers.append(AOP_JBOSS5_DEPLOYER);
		IPath client = homePath.append(CLIENT);
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.add(getEntry(aopDeployer.append(JBOSS6_AOP_ASPECTS_JAR)));
		list.add(getEntry(aopDeployer.append(JBOSS6_AS_ASPECT_LIBRARY_JAR)));
		list.add(getEntry(deployers.append(JB6_EJB3_ENDPOINT_DEPLOYER_JAR)));
		list.add(getEntry(deployers.append(JB6_EJB3_METRICS_DEPLOYER_JAR)));
		list.add(getEntry(client.append(jboss_ejb3_common_client)));
		list.add(getEntry(client.append(jboss_ejb3_core_client)));
		list.add(getEntry(client.append(jboss_ejb3_ext_api_impl)));
		list.add(getEntry(client.append(jboss_ejb3_ext_api)));
		list.add(getEntry(client.append(jboss6_ejb3_proxy_spi_client)));
		list.add(getEntry(client.append(jboss6_ejb3_proxy_impl_client)));
		list.add(getEntry(client.append(jboss_ejb3_proxy_clustered_client)));
		list.add(getEntry(client.append(jboss_ejb3_security_client)));
		if (homePath.append(CLIENT).append(JB50_HIBERNATE_ANNOTATIONS_JAR).toFile().exists())
			list.add(getEntry(homePath.append(CLIENT).append(JB50_HIBERNATE_ANNOTATIONS_JAR)));
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	protected static IClasspathEntry[] get70Jars(IPath homePath) throws FileNotFoundException {
		return get70JarsFromBase(homePath.append(MODULES));
	}

	protected static IClasspathEntry[] getEap61Jars(IPath homePath) throws FileNotFoundException {
		return get70JarsFromBase(homePath.append(MODULES).append("system").append("layers").append("base")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected static IClasspathEntry[] get70JarsFromBase(IPath base) throws FileNotFoundException {
		IPath apiFolder = base.append("javax").append("ejb").append("api").append("main"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IPath jbossEjb3Folder = base.append("org").append("jboss").append("ejb3").append("main"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IPath jbossASEjb3Folder = base.append("org").append("jboss").append("as").append("ejb3").append("main"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		IPath api = findJarFile(apiFolder);
		IPath jbossEjb3 = findJarFile(jbossEjb3Folder);
		IPath jbossASEjb3 = findJarFile(jbossASEjb3Folder);
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		if (api != null)
			list.add(getEntry(api));
		if (jbossEjb3 != null)
			list.add(getEntry(jbossEjb3));
		if (jbossASEjb3 != null)
			list.add(getEntry(jbossASEjb3));
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	protected static IPath findJarFile(IPath folder) {
		String[] names = folder.toFile().list();
		for (int i = 0; i < names.length; i++) {
			if (names[i].endsWith(".jar")) //$NON-NLS-1$
				return folder.append(names[i]);
		}
		return null;
	}

	protected static IClasspathEntry getEntry(IPath path) throws FileNotFoundException {
		if (!path.toFile().exists())
			throw new FileNotFoundException();
		return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
	}

}
