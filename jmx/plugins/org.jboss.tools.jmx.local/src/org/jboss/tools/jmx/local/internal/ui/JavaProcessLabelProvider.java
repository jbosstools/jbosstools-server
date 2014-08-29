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

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.local.internal.Activator;
import org.jboss.tools.jmx.local.internal.LocalVMSharedImages;
import org.jboss.tools.jmx.local.ui.JVMLabelProviderDelegate;

public class JavaProcessLabelProvider implements JVMLabelProviderDelegate {
	public boolean accepts(IActiveJvm jvm) {
		// checked last, so always accepts
		return true;
	}
	public Image getImage(IActiveJvm jvm) {
		return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
	}
	public String getDisplayString(IActiveJvm jvm) {
		if( isBlank(jvm.getMainClass())) 
			return "Java Process";
		String main = jvm.getMainClass();
		String alias = JVMConnectionLabelProvider.vmAliasMap.get(main);
		return alias == null ? main : alias;
	}
	static boolean isBlank(String text) {
		return text == null || text.trim().length() == 0;
	}

}