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
package org.jboss.tools.jmx.local.internal;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.osgi.framework.Bundle;

public class LocalVMSharedImages extends BaseUISharedImages {

	public static final String CONTAINER_GIF = "image/container.gif";//$NON-NLS-1$
	public static final String CONTAINER_PNG = "image/container.png";//$NON-NLS-1$
	public static final String ECLIPSE_PNG = "image/eclipse16.png";//$NON-NLS-1$

	private static LocalVMSharedImages shared;
	public static LocalVMSharedImages getDefault() {
		if( shared == null )
			shared = new LocalVMSharedImages();
		return shared;
	}
	
	
	public LocalVMSharedImages(Bundle pluginBundle) {
		super(pluginBundle);
		addImage(CONTAINER_GIF, CONTAINER_GIF);
		addImage(CONTAINER_PNG, CONTAINER_PNG);
		addImage(ECLIPSE_PNG, ECLIPSE_PNG);		
	}
	
	private LocalVMSharedImages() {
		this(JMXUIActivator.getDefault().getBundle());
	}

	public static Image getImage(String k) {
		return getDefault().image(k);
	}
}
