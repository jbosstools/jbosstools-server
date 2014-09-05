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

public class EclipseProcessLabelProvider implements JVMLabelProviderDelegate {
	public boolean accepts(IActiveJvm jvm) {
		String main = jvm.getMainClass();
		if( main.contains("org.eclipse.equinox.launcher_")) {
			return true;
		}
		if( main.equals("org.eclipse.equinox.launcher.Main"))
			return true;
		return false;
	}
	public Image getImage(IActiveJvm jvm) {
		return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.ECLIPSE_PNG);
	}
	public String getDisplayString(IActiveJvm jvm) {
		String main = jvm.getMainClass();
		if( main.contains("org.eclipse.equinox.launcher_")) {
			return "Eclipse Runtime Workbench";
		}
		if( main.equals("org.eclipse.equinox.launcher.Main"))
			return "Eclipse";
		return null;
	}
}