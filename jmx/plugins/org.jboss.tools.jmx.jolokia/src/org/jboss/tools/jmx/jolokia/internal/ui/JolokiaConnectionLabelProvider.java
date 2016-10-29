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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.jolokia.JolokiaConnectionWrapper;

public class JolokiaConnectionLabelProvider extends LabelProvider implements ILabelProvider {
	
	public Image getImage(Object element) {
		Image ret = null;
		if( element instanceof JolokiaConnectionWrapper) {
			// TODO return a jolokia icon
		}
		return ret;
	}
	
	
	@Override
	public String getText(Object element) {
		if( element instanceof JolokiaConnectionWrapper) {
			return element.toString(); // TODO 
		}
		return null;
	}

}
