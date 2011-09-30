/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.jboss.ide.eclipse.as.openshift.ui.internal.common.ImageRepository;

public class OpenshiftImages {

	private static final String ICONS_FOLDER = "icons/";

	private static final ImageRepository repo = 
			new ImageRepository(ICONS_FOLDER, OpenshiftUIActivator.getDefault(), OpenshiftUIActivator.getDefault().getImageRegistry());

	public static final ImageDescriptor OPENSHIFT_LOGO_DARK = repo.create("openshift-logo-dark.png"); //$NON-NLS-1$ 
	public static final ImageDescriptor OPENSHIFT_LOGO_WHITE = repo.create("openshift-logo-white.png"); //$NON-NLS-1$ 

	public static final ImageDescriptor OK = repo.create("ok.png"); //$NON-NLS-1$ 
	public static final ImageDescriptor ERROR = repo.create("error.png"); //$NON-NLS-1$ 
	public static final ImageDescriptor WARNING = repo.create("warning.png"); //$NON-NLS-1$ 
	
	
}
