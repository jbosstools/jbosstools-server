/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.views.navigator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public class EmbedableLabelProvider extends LabelProvider {
	private MBeanExplorerLabelProvider delegate;
	public EmbedableLabelProvider() {
		delegate = new MBeanExplorerLabelProvider();
	}
	
	public void dispose() {
		delegate.dispose();
	}
	
	public String getText(Object element) {
		if( element instanceof IConnectionWrapper ) {
			return "JMX";  //$NON-NLS-1$
		}
		return delegate.getText(element);
	}
	
	public Image getImage(Object element) {
		return delegate.getImage(element);
	}
}
