/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.jboss.ide.eclipse.as.wtp.core.vcf.JBTVirtualComponent;

public class SARVirtualComponent extends JBTVirtualComponent implements
		IComponentImplFactory {
	public SARVirtualComponent() {
		super();
	}
	
	public SARVirtualComponent(IProject aProject, Path path) {
		super(aProject, path);
	}

	public IVirtualComponent createComponent(IProject aProject) {
		return new SARVirtualComponent(aProject, new Path("/")); //$NON-NLS-1$
	}
}
