/******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Konstantin Komissarchik - initial API and implementation
 *    IBM Corporation - Support for all server types
 * Patch:
 *    Rob Stryker 10/31/06
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.IJavaRuntime;
import org.eclipse.jst.server.core.internal.JavaServerPlugin;
import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
import org.eclipse.jst.server.core.internal.RuntimeClasspathProviderWrapper;
import org.eclipse.jst.server.core.internal.RuntimeComponentProviderWrapper;
import org.eclipse.jst.server.core.internal.RuntimeFacetMapping;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeBridge;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Runtime;

public class JBossRuntimeBridge implements IRuntimeBridge {
	protected static final String CLASSPATH = "classpath";

	protected static Map mappings = new HashMap();

	static {
		initialize();
	}

	private static void addMapping(String id, String id2, String version) {
		try {
			Object list = mappings.get(id);
			ArrayList list2 = (list == null ? new ArrayList() : (ArrayList)list);
			list2.add( RuntimeManager.getRuntimeComponentType(id2).getVersion(version));
			mappings.put(id, list2);
		} catch (Exception e) {
			// ignore
		}
	}

	private static void initialize() {
		RuntimeFacetMapping[] rfms = JavaServerPlugin.getRuntimeFacetMapping();
		int size = rfms.length;
		for (int i = 0; i < size; i++)
			addMapping(rfms[i].getRuntimeTypeId(), rfms[i].getRuntimeComponent(), rfms[i].getVersion());

		// generic runtimes
		addMapping("org.eclipse.jst.server.generic.runtime.weblogic81", "org.eclipse.jst.server.generic.runtime.weblogic", "8.1");

		addMapping("org.eclipse.jst.server.generic.runtime.weblogic90", "org.eclipse.jst.server.generic.runtime.weblogic", "9.0");

		addMapping("org.eclipse.jst.server.generic.runtime.jboss323", "org.eclipse.jst.server.generic.runtime.jboss", "3.2.3");

		addMapping("org.eclipse.jst.server.generic.runtime.jonas4", "org.eclipse.jst.server.generic.runtime.jonas", "4.0");
		
		addMapping("org.eclipse.jst.server.generic.runtime.oracle1013", "org.eclipse.jst.server.generic.runtime.oracle", "10.1.3");
		
		addMapping("org.eclipse.jst.server.generic.runtime.websphere.6", "org.eclipse.jst.server.generic.runtime.websphere", "6.0");
	}

	public Set getExportedRuntimeNames() throws CoreException {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		Set result = new HashSet(runtimes.length);
		
		for (int i = 0; i < runtimes.length; i++) {
			IRuntime runtime = runtimes[i];
			IRuntimeType runtimeType = runtime.getRuntimeType();
			if (runtimeType != null && mappings.containsKey(runtimeType.getId())) {
				result.add(runtime.getName());
			}
		}
		
		return result;
	}

	public IStub bridge(String name) throws CoreException {
		if (name == null)
			throw new IllegalArgumentException();
		
		IRuntime[] runtimes = ServerCore.getRuntimes();
		int size = runtimes.length;
		for (int i = 0; i < size; i++) {
			if (runtimes[i].getName().equals(name))
				return new Stub(runtimes[i]);
		}
		return null;
	}

	private static class Stub implements IStub {
		private IRuntime runtime;
		protected int timestamp = -1;
		protected IVMInstall vmInstall;
		protected String jvmver;

		public Stub(IRuntime runtime) {
			this.runtime = runtime;
		}

		public List getRuntimeComponents() {
			List components = new ArrayList(2);
			if (runtime == null)
				return components;
			
			// define server runtime component
			String typeId = runtime.getRuntimeType().getId();
			
			Map properties = new HashMap(5);
			if (runtime.getLocation() != null)
				properties.put("location", runtime.getLocation().toPortableString());
			else
				properties.put("location", "");
			properties.put("name", runtime.getName());
			properties.put("type", runtime.getRuntimeType().getName());
			properties.put("id", runtime.getId());
			
			RuntimeClasspathProviderWrapper rcpw = JavaServerPlugin.findRuntimeClasspathProvider(runtime.getRuntimeType());
			if (rcpw != null) {
				IPath path = new Path(RuntimeClasspathContainer.SERVER_CONTAINER);
				path = path.append(rcpw.getId()).append(runtime.getId());
				properties.put(CLASSPATH, path.toPortableString());
			}
			

			
			if( mappings.containsKey(typeId)) {
				ArrayList mappedComponents = (ArrayList)mappings.get(typeId);
				for( int i = 0; i < mappedComponents.size(); i++ ) {
					IRuntimeComponentVersion mapped = (IRuntimeComponentVersion) mappedComponents.get(i);
					components.add(RuntimeManager.createRuntimeComponent(mapped, properties));
				}
			}
			
			// define JRE component
			IJavaRuntime javaRuntime = (IJavaRuntime) runtime.loadAdapter(IJavaRuntime.class, null);
			if (javaRuntime != null) {
				if (timestamp != ((Runtime) runtime).getTimestamp()) {
					vmInstall = null;
					jvmver = null;
					timestamp = ((Runtime) runtime).getTimestamp();
				}
				if (vmInstall == null)
					vmInstall = javaRuntime.getVMInstall(); 
				if (jvmver == null) {
					IVMInstall2 vmInstall2 = (IVMInstall2) vmInstall;
					if (vmInstall2 != null)
						jvmver = vmInstall2.getJavaVersion();
				}
				IRuntimeComponentVersion rcv;
				
				String vmInstallName;
				if (vmInstall != null)
					vmInstallName = vmInstall.getName();
				else
					vmInstallName = "Unknown";
				
				if (jvmver == null) {
					JavaServerPlugin.logWarning("Could not determine VM version for: " + vmInstallName);
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("6.0");
				} else if (jvmver.startsWith("1.3"))
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("1.3");
				else if (jvmver.startsWith("1.4"))
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("1.4");
				else if (jvmver.startsWith("1.5") || jvmver.startsWith("5.0"))
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("5.0");
				else if (jvmver.startsWith("1.6") || jvmver.startsWith("6.0"))
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("6.0");
				else {
					JavaServerPlugin.logWarning("Invalid Java version: " + vmInstallName + ", " + jvmver);
					rcv = RuntimeManager.getRuntimeComponentType("standard.jre").getVersion("6.0");
				}
				
				properties = new HashMap(3);
				if (vmInstallName != null)
					properties.put("name", vmInstallName);
				else
					properties.put("name", "-");
				
				if (vmInstall == null || isUsingDefaultJRE(javaRuntime))
					properties.put(CLASSPATH, new Path(JavaRuntime.JRE_CONTAINER).toPortableString());
				else
					properties.put(CLASSPATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
				components.add(RuntimeManager.createRuntimeComponent(rcv, properties));
			}
			
			RuntimeComponentProviderWrapper componentProvider = JavaServerPlugin.findRuntimeComponentProvider(runtime.getRuntimeType());
			if (componentProvider != null) {
				List list = componentProvider.getComponents(runtime);
				if (list != null)
					components.addAll(list);
			}
			
			return components;
		}

		public Map getProperties() {
			if (runtime == null)
				return new HashMap(0);
			return Collections.singletonMap("id", runtime.getId());
		}
	}

	protected static boolean isUsingDefaultJRE(IJavaRuntime javaRuntime) {
		try {
			Method m = javaRuntime.getClass().getMethod("isUsingDefaultJRE", null);
			Object o = m.invoke(javaRuntime, null);
			Boolean b = (Boolean) o;
			return b.booleanValue();
		} catch (Throwable t) {
			// ignore - method not found
		}
		return false;
	}

}
