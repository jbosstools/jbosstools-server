/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.views.navigator;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.Messages;

public class JMXConnectionDecorator extends LabelProvider implements ILightweightLabelDecorator {
	public void decorate(Object element, IDecoration decoration) {
		String decoration2 = getDecoration(element);
		if( decoration2 != null ) {
			decoration.addSuffix(decoration2);
		}
	}
	
	public static String getDecoration(Object element) {
		String ret = null;
		if( element instanceof IConnectionWrapper) {
			boolean connected = ((IConnectionWrapper)element).isConnected();
			return connected ? Messages.StateConnected : Messages.StateDisconnected;
		}
		return ret;
	}
}
