/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dom4j.Document;
import org.dom4j.tree.DefaultElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.EjbDeploymentVerifier;
import org.jboss.ide.eclipse.as.core.util.ASDebug;


/**
 * This class should have no state whatseoever. Merely factory methods.
 * @author rstryker
 *
 */
public class EjbModuleFactory extends JBossModuleFactory {
	
	private final static String EJB_MODULE_TYPE = "jboss.ejb";
	private final static String EJB_MODULE_VERSION_3_2 = "3.2";
	private final static String EJB_DESCRIPTOR = "META-INF/ejb-jar.xml";
	private final static String JBOSS_EJB_DESCRIPTOR = "META-INF/jboss.xml";
	

	
	public void initialize() {
	}


	/**
	 * TODO: Expand to check if it's a jar file and if it contains the proper xml file
	 */
	public boolean supports(IResource resource) {
		if( resource.getFileExtension() == null ) return false;
		if( !"jar".equals(resource.getFileExtension())) return false;
		
		if( !resource.exists()) return false;

		try {
			File f = resource.getLocation().toFile();
			JarFile jf = new JarFile(f);
			
			JarEntry entry = jf.getJarEntry(EJB_DESCRIPTOR);
			jf.close();
			if( entry == null ) return false;			
		} catch( IOException ioe ) {
			return false;
		}
		
		
		return true;
	}


	
	protected IModule acceptAddition(IResource resource) {
		 //return null if I don't support it
		if( !supports(resource)) 
			return null;

		
		// otherwise create the module
		IModule module = createModule(getPath(resource), resource.getName(), 
				EJB_MODULE_TYPE, EJB_MODULE_VERSION_3_2, resource.getProject());
		
		
		EjbModuleDelegate delegate = new EjbModuleDelegate();
		delegate.initialize(module);
		delegate.setResource(resource);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(getPath(resource), module);
		moduleToDelegate.put(module, delegate);
		
		return module;
	}
	
	public Object getLaunchable(JBossModuleDelegate delegate) {
		if( !(delegate instanceof EjbModuleDelegate) ) {
			// If I didn't make this guy... get rid of him!
			return null;
		}
		return new EjbDeploymentVerifier(delegate);
	}
	
	
	
	
	public class EjbModuleDelegate extends JBossModuleDelegate {

		
		public IModule[] getChildModules() {
			return new IModule[] { };
		}

		public void initialize() {
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[] { };
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "Deployment is valid", null);
		}
		
		public String[] getBeanJNDINames() {
			// First get all ejb names from the ejb descriptor.
			ArrayList jndiNames = new ArrayList();
			
			String[] ejbNames = getXpathValues(EJB_DESCRIPTOR, "/ejb-jar/enterprise-beans/*/ejb-name");
			StringPair[] pairs = getEjbJndiPairs();
			for( int i = 0; i < ejbNames.length; i++ ) {
				boolean found = false;
				for( int j = 0; j < pairs.length && !found; j++ ) {
					if( pairs[j].getEjbName().equals(ejbNames[i])) {
						// add the jndi name
						jndiNames.add(pairs[j].getJndiName());
						found = true;
					}
				}
				if( !found ) {
					jndiNames.add(ejbNames[i]);
				}
			}
			
			String[] retval = new String[jndiNames.size()];
			jndiNames.toArray(retval);
			return retval;
		}

		

		private StringPair[] getEjbJndiPairs() {
			ArrayList pairs = new ArrayList();
			Document doc = getDocument(JBOSS_EJB_DESCRIPTOR);
			if( doc == null ) return new StringPair[] { };

			List l = doc.selectNodes("/jboss/enterprise-beans/*");
			Iterator i = l.iterator();
			while(i.hasNext()) {
				DefaultElement de = (DefaultElement)i.next();
				List beanNameList = de.selectNodes("ejb-name");
				List beanJndiNameList = de.selectNodes("jndi-name");
				String beanName = null;
				String jndiName = null;
				
				if(beanNameList.size() == 1) {
					beanName = ((DefaultElement)beanNameList.get(0)).getText();
				}
				if( beanJndiNameList.size() == 1 ) {
					jndiName = ((DefaultElement)beanJndiNameList.get(0)).getText();
				}
				pairs.add(new StringPair(beanName, jndiName));
			}
			
			StringPair[] retval = new StringPair[pairs.size()];
			pairs.toArray(retval);
			return retval;
		}
		
		private class StringPair {
			private String ejbName;
			private String jndiName;
			public StringPair(String ejbName, String jndiName) {
				this.ejbName = ejbName;
				this.jndiName = jndiName;
			}
			public String getEjbName() {
				return ejbName;
			}
			public String getJndiName() {
				return jndiName;
			}
		}

	}

	
}
