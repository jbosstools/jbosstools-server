/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.internal.ui;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.jboss.tools.jmx.jolokia.internal.Activator;
import org.osgi.framework.Bundle;

public class JolokiaSharedImages extends BaseUISharedImages {

	public static final String JOLOKIA_BAN = "image/jolokia_ban.png";//$NON-NLS-1$

	private static JolokiaSharedImages shared;
	public static JolokiaSharedImages getDefault() {
		if( shared == null )
			shared = new JolokiaSharedImages();
		return shared;
	}
	
	
	public JolokiaSharedImages(Bundle pluginBundle) {
		super(pluginBundle);
		addImage(JOLOKIA_BAN, JOLOKIA_BAN);
	}
	
	private JolokiaSharedImages() {
		this(Activator.getDefault().getBundle());
	}

	public static Image getImage(String k) {
		return getDefault().image(k);
	}
}
