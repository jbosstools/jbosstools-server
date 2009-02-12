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

package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;

/**
 * @author Marshall
 * @author Rob Stryker 
 */
public class EJB3ClasspathContainer implements IClasspathContainer, ClasspathConstants {
   public static final String CONTAINER_ID = "org.jboss.ide.eclipse.as.classpath.core.ejb3.classpathContainer"; //$NON-NLS-1$

   public static final String DESCRIPTION = "JBoss EJB3 Libraries";

   public static final QualifiedName JBOSS_EJB3_CONFIGURATION = new QualifiedName(
         "org.jboss.ide.eclipse.ejb3.wizards.core.classpath", "jboss-ejb3-configuration"); //$NON-NLS-1$ //$NON-NLS-2$

   protected IJavaProject javaProject;
   protected JBossServer jbossServer;
   protected IPath path;

   protected IPath configPath = new Path(""); //$NON-NLS-1$
   protected IPath homePath = null;
   

   public EJB3ClasspathContainer(IPath path, IJavaProject project) {
	  this.path = path;
	  this.javaProject = project;

      try {
         String configName = path.segment(1);
         IServer servers[] = ServerCore.getServers();

         if (configName == null) {
            // old classpath container, try finding the persisten property
            configName = project.getProject().getPersistentProperty(JBOSS_EJB3_CONFIGURATION);
            if (configName != null) {
               // go ahead and remove the persistent property
               project.getProject().setPersistentProperty(JBOSS_EJB3_CONFIGURATION, null);
            }
         }

         for (int i = 0; i < servers.length; i++) {
            if (servers[i].getName().equals(configName))  {
            	jbossServer = (JBossServer) servers[i].loadAdapter(JBossServer.class, new NullProgressMonitor());
            	try {
            	homePath = jbossServer.getServer().getRuntime().getLocation();
            	configPath = new Path(jbossServer.getConfigDirectory());
            	} catch( Exception e ) { e.printStackTrace(); }
            	break;
            }
         }
      } catch (CoreException e) {
      }

   }

   public String getDescription() {
	   return "JBoss EJB 3.0 Libraries";
   }

   public JBossServer getJBossServer() {
      return jbossServer;
   }

   public void setJBossServer(JBossServer jbossServer) {
      this.jbossServer = jbossServer;
   }

   public int getKind() {
      return K_APPLICATION;
   }

   public IPath getPath() {
      return path;
   }

   public IClasspathEntry[] getClasspathEntries() {
      ArrayList entries = new ArrayList();
      String id = jbossServer.getServer().getServerType().getRuntimeType().getId();
      if( id.equals(AS_40)) return get40Jars();
      if( id.equals(AS_42)) return get42Jars();
      return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
   }

   public IClasspathEntry[] get40Jars() {
		ArrayList list = new ArrayList();

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
   
   public IClasspathEntry[] get42Jars() {
		ArrayList list = new ArrayList();

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
   

	protected IClasspathEntry getEntry(IPath path) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
	}

}
