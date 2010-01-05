/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

/**
 * This is a class that would ultimately try to respond to different facets
 * by providing different jars to the classpath, jars specific to the added
 * facet. 
 * 
 * It is currently only used by the EJB30SupportVerifier, or by 
 * the ProjectRuntimeClasspathProvider (front) when the added facet
 * is jst.java. 
 * 
 * @author Rob Stryker
 *
 */
public class WebtoolsProjectJBossClasspathContainerInitializer extends
		ClasspathContainerInitializer implements ClasspathConstants {
	
	public static final IProjectFacet JST_JAVA_FACET = ProjectFacetsManager.getProjectFacet(FACET_JST_JAVA);
	public static final IProjectFacet WEB_FACET = ProjectFacetsManager.getProjectFacet(FACET_WEB);
	public static final IProjectFacet EJB_FACET = ProjectFacetsManager.getProjectFacet(FACET_EJB);
	public static final IProjectFacet EAR_FACET = ProjectFacetsManager.getProjectFacet(FACET_EAR);
	public static final IProjectFacet UTILITY_FACET = ProjectFacetsManager.getProjectFacet(FACET_UTILITY);
	public static final IProjectFacet CONNECTOR_FACET = ProjectFacetsManager.getProjectFacet(FACET_CONNECTOR);
	public static final IProjectFacet APP_CLIENT_FACET = ProjectFacetsManager.getProjectFacet(FACET_APP_CLIENT);


	public WebtoolsProjectJBossClasspathContainerInitializer() {
		// TODO Auto-generated constructor stub
	}

	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		WebtoolsProjectJBossClasspathContainer container = new WebtoolsProjectJBossClasspathContainer(containerPath);
		
		JavaCore.setClasspathContainer(containerPath, 
				new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
	}
	
	public IClasspathEntry[] getEntries(IPath path) {
		return new WebtoolsProjectJBossClasspathContainer(path).getClasspathEntries();
	}
	
	
	
	
	public static class WebtoolsProjectJBossClasspathContainer implements IClasspathContainer {
		private IPath path;
		private IClasspathEntry[] entries = null;

		public WebtoolsProjectJBossClasspathContainer(IPath path) {
			this.path = path;
		}
				
		public String getDescription() {
			if( path.segmentCount() < 4 ) return Messages.WebtoolsProjectJBossClasspathContainerInitializer_jboss_runtimes;
			String pathSegments = path.segment(2) + " : " + path.segment(3); //$NON-NLS-1$
			return MessageFormat.format(Messages.WebtoolsProjectJBossClasspathContainerInitializer_jboss_runtimes_path, pathSegments);
		}

		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}
		
		
		
		public IClasspathEntry[] getClasspathEntries() {
			if( entries == null ) {
				loadClasspathEntries();
				if( entries == null ) 
					return new IClasspathEntry[0];
			}
			return entries;
		}
		
		private void loadClasspathEntries() {
			if( path.segmentCount() < 4 ) return;
			String runtimeId = path.segment(1);
			String facetId = path.segment(2);
			String facetVersion = path.segment(3);
			
			
			if( runtimeId == null ) return;
			
			IRuntime runtime = ServerCore.findRuntime(runtimeId);
			if( runtime == null ) return;
			
			IJBossServerRuntime  jbRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, null);

			if( jbRuntime != null ) {

				String serverHome = runtime.getLocation().toOSString();
				String configName = jbRuntime.getJBossConfiguration();
				
				String jbossVersion = jbRuntime.getRuntime().getRuntimeType().getVersion();
				
				entries = loadClasspathEntries2(runtimeId, facetId, facetVersion, 
						serverHome, configName, jbossVersion, jbRuntime);
			}
		}

		protected IClasspathEntry[] loadClasspathEntries2(String runtimeId, String facetId, 
				String facetVersion, String serverHome, String configName, String jbVersion, 
				IJBossServerRuntime jbsRuntime) {
			if( facetId.equals(JST_JAVA_FACET.getId())) {
				return loadJREClasspathEntries(jbsRuntime);
			} else if(V5_0.equals(jbVersion)) {
				return loadClasspathEntriesDefault(facetId, facetVersion, serverHome, configName);
			} else if( V4_2.equals(jbVersion)) {
				return loadClasspathEntries42(facetId, facetVersion, serverHome, configName);
			} else if( V4_0.equals(jbVersion)) {
				return loadClasspathEntries40(facetId, facetVersion, serverHome, configName);
			} else if( V3_2.equals(jbVersion))  
				return loadClasspathEntries32( facetId, facetVersion, serverHome, configName);
			return loadClasspathEntriesDefault(facetId, facetVersion, serverHome, configName);
		}
		
		protected boolean isEjb30(String facetId, String facetVersion) {
			if( facetId.equals(EJB_FACET.getId()) && facetVersion.equals(V3_0)) 
				return true;
			return false;
		}
		
		protected IClasspathEntry getEntry(IPath path) {
			return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
		}
		protected IClasspathEntry[] getEntries(IPath folder) {
			String[] files = folder.toFile().list();
			ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			for( int i = 0; i < files.length; i++ ) {
				if( files[i].endsWith(EXT_JAR)) {
					list.add(getEntry(folder.append(files[i])));
				}
			}
			return list.toArray(new IClasspathEntry[list.size()]);
		}
		protected IClasspathEntry[] loadJREClasspathEntries(IJBossServerRuntime jbsRuntime) {
			IVMInstall vmInstall = jbsRuntime.getVM();
			if (vmInstall != null) {
				String name = vmInstall.getName();
				String typeId = vmInstall.getVMInstallType().getId();
				return new IClasspathEntry[] { JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER).append(typeId).append(name)) };
			}
			return null;
		}

		protected IClasspathEntry[] loadClasspathEntries42(String facetId, String facetVersion, String serverHome, String configName) {
			IPath homePath = new Path(serverHome);
			IPath configPath = homePath.append(SERVER).append(configName);
			ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			if (facetId.equals(WEB_FACET.getId())) {
				IPath jsfDir = configPath.append(DEPLOY).append(JBOSS_WEB_DEPLOYER).append(JSF_LIB);
				list.add(getEntry(configPath.append(LIB).append(JSP_API_JAR)));
				list.add(getEntry(homePath.append(CLIENT).append(SERVLET_API_JAR)));
				list.add(getEntry(jsfDir.append(JSF_API_JAR)));
				list.add(getEntry(jsfDir.append(JSF_IMPL_JAR)));
			} else if( facetId.equals(EJB_FACET.getId()) && !isEjb30(facetId, facetVersion)) {
					list.add(getEntry(homePath.append(CLIENT).append(JBOSS_J2EE_JAR)));
			} else if( isEjb30(facetId, facetVersion)) {
				// path roots
				IPath deploy = configPath.append(DEPLOY);
				IPath deployer = deploy.append(EJB3_DEPLOYER);
				IPath aopDeployer = deploy.append(AOP_JDK5_DEPLOYER);
				IPath client = homePath.append(CLIENT);
				
				list.add(getEntry(configPath.append(LIB).append(JBOSS_EJB3X_JAR)));
				list.add(getEntry(deployer.append(JBOSS_EJB3_JAR)));
				list.add(getEntry(deployer.append(JBOSS_ANNOTATIONS_EJB3_JAR)));
				
				// aop
				list.add(getEntry(aopDeployer.append(AOP_JDK5_DEPLOYER)));
				list.add(getEntry(aopDeployer.append(JBOSS_ASPECT_LIBRARY_JDK5_0)));
				
				// hibernate
				list.add(getEntry(homePath.append(CLIENT).append(HIBERNATE_CLIENT_JAR)));
				
				// persistence jar
				list.add(getEntry(client.append(EJB3_PERSISTENCE_JAR)));
				
			} else if( facetId.equals(EAR_FACET.getId())) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSS_J2EE_JAR)));
			} else if( facetId.equals(APP_CLIENT_FACET.getId())) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSSALL_CLIENT_JAR)));
			}
			return list.toArray(new IClasspathEntry[list.size()]);
		}

		protected IClasspathEntry[] loadClasspathEntries40(String facetId, String facetVersion, String serverHome, String configName) {
			IPath homePath = new Path(serverHome);
			IPath configPath = homePath.append(SERVER).append(configName);
			ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			if (facetId.equals(WEB_FACET.getId())) {
				IPath jsfDir = configPath.append(DEPLOY).append(JBOSSWEB_TOMCAT55_SAR).append(JSF_LIB);
				list.add(getEntry(configPath.append(LIB).append(JAVAX_SERVLET_JSP_JAR)));
				list.add(getEntry(homePath.append(CLIENT).append(JAVAX_SERVLET_JAR)));
				list.addAll(Arrays.asList(getEntries(jsfDir)));
			} else if( facetId.equals(EJB_FACET.getId()) && !isEjb30(facetId, facetVersion)) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSS_J2EE_JAR)));
			} else if( isEjb30(facetId, facetVersion)) {
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
				
			} else if( facetId.equals(EAR_FACET.getId())) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSS_J2EE_JAR)));
			} else if( facetId.equals(APP_CLIENT_FACET.getId())) {
				list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(homePath.append(CLIENT).append(JBOSSALL_CLIENT_JAR)).getClasspathEntry());
			}
			return list.toArray(new IClasspathEntry[list.size()]);
		}

		
		protected IClasspathEntry[] loadClasspathEntries32(String facetId, String facetVersion, String serverHome, String configName) {
			IPath homePath = new Path(serverHome);
			IPath configPath = homePath.append(SERVER).append(configName);
			ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			if (facetId.equals(WEB_FACET.getId())) {
				IPath p = configPath.append(DEPLOY).append(JBOSSWEB_TOMCAT_50_SAR);
				list.add(getEntry(p.append(JSP_API_JAR)));
				list.add(getEntry(p.append(SERVLET_API_JAR)));
			} else if( (facetId.equals(EJB_FACET.getId()) && !isEjb30(facetId, facetVersion))
					|| facetId.equals(EAR_FACET.getId()) ) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSS_J2EE_JAR)));
			} else if( facetId.equals(APP_CLIENT_FACET.getId())) {
				list.add(getEntry(homePath.append(CLIENT).append(JBOSSALL_CLIENT_JAR)));
			}
			return list.toArray(new IClasspathEntry[list.size()]);
		}
		protected IClasspathEntry[] loadClasspathEntriesDefault(String facetId, String facetVersion, String serverHome, String configName) {
			return new IClasspathEntry[0];
		}

		protected IClasspathEntry[] pathsAsEntries(IPath[] paths) {
			IClasspathEntry[] entries = new IClasspathEntry[paths.length];
			for( int i = 0; i < paths.length; i++ )
				entries[i] = getEntry(paths[i]);
			return entries;
		}
		
	}

}
