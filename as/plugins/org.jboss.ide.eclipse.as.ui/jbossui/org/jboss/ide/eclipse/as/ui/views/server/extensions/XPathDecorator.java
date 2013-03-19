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
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;

public class XPathDecorator extends LabelProvider implements ILightweightLabelDecorator {
	public void decorate(Object element, IDecoration decoration) {
		String decoration2 = getDecoration(element);
		if( decoration2 != null ) {
			decoration.addSuffix(decoration2);
		}
	}
	
	public static String getDecoration(Object element) {
		if( element instanceof XPathQuery) {
			XPathResultNode[] nodes = XPathModel.getResultNodes((XPathQuery)element);
			if(nodes.length == 1 ) {
				return "   " + nodes[0].getText(); //$NON-NLS-1$
			} 
		}

		if( element instanceof XPathFileResult ) {
			XPathResultNode[] nodes = ((XPathFileResult)element).getChildren();
			if( nodes.length == 1 )
				return "   " + nodes[0].getText(); //$NON-NLS-1$
		}
		
		if( element instanceof XPathResultNode ) {
			return "   " + ((XPathResultNode)element).getText(); //$NON-NLS-1$
		}
		return null;
	}
}