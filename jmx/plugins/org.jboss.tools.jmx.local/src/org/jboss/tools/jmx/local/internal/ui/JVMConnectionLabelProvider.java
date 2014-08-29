/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.local.internal.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.local.internal.Activator;
import org.jboss.tools.jmx.local.internal.JmxLocalExtensionManager;
import org.jboss.tools.jmx.local.internal.JvmConnectionWrapper;
import org.jboss.tools.jmx.local.internal.LocalVMSharedImages;
import org.jboss.tools.jmx.local.ui.JVMLabelProviderDelegate;

public class JVMConnectionLabelProvider extends LabelProvider implements ILabelProvider {
	protected static final Map<String,String> vmAliasMap = new HashMap<String, String>();
	
	static {
		// Legacy aliases, backup for now
		vmAliasMap.put("com.intellij.rt.execution.application.AppMain", "idea");
		vmAliasMap.put("org.jetbrains.idea.maven.server.RemoteMavenServer", "idea maven server");
		vmAliasMap.put("scala.tools.nsc.MainGenericRunner", "scala repl");
	}
	
	private JVMLabelProviderDelegate[] jvmConnectionLabelProviders = JmxLocalExtensionManager.getDefault().getJvmConnectionLabelProviders();	

	private JVMLabelProviderDelegate findProvider(IActiveJvm jvm) {
		for( int i = 0; i < jvmConnectionLabelProviders.length; i++ ) {
			if( jvmConnectionLabelProviders[i].accepts(jvm))
				return jvmConnectionLabelProviders[i];
		}
		return null;
	}
	


	static String getNameFromAliasMap(String displayName) {
		Set<Entry<String, String>> entrySet = vmAliasMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			if (displayName.startsWith(key)) {
				return (entry.getValue() + displayName.substring(key.length()));
			}
		}
		return displayName;
	}
	
	public Image getImage(Object element) {
		Image ret = null;
		if( element instanceof JvmConnectionWrapper) {
			IActiveJvm activeJvm = ((JvmConnectionWrapper)element).getActiveJvm();
			JVMLabelProviderDelegate provider  = findProvider(activeJvm);
			if( provider != null ) {
				ret = provider.getImage(activeJvm);
			}
			if( ret == null ) {
				ret = Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
			}
		}
		return ret;
	}
	
	
	@Override
	public String getText(Object element) {
		if( element instanceof JvmConnectionWrapper) {
			IActiveJvm activeJvm = ((JvmConnectionWrapper)element).getActiveJvm();
			JVMLabelProviderDelegate provider  = findProvider(activeJvm);
			String displayName;
			if( provider != null ) {
				displayName = provider.getDisplayString(activeJvm);
			} else {
				displayName = activeJvm.getMainClass();
				displayName = getNameFromAliasMap(displayName);
			}
			// include pid in name
			displayName += " [" + activeJvm.getPid() + "]";
			return displayName;
		}
		return null;
	}

}
