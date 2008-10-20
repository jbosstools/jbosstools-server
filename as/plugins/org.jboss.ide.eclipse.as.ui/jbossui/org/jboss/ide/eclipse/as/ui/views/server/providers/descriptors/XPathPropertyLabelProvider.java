/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.views.server.providers.descriptors;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class XPathPropertyLabelProvider extends LabelProvider implements ITableLabelProvider {
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {
		if( element instanceof XPathQuery) {
			if( columnIndex == 0 ) return ((XPathQuery)element).getName();
			if( columnIndex == 1 ) {
				XPathResultNode[] nodes = getResultNodes(((XPathQuery)element));
				if( nodes.length == 1 )
				return nodes[0].getText();
			}
		}

		if( element instanceof XPathFileResult ) {
			XPathFileResult result = (XPathFileResult)element;
			if( columnIndex == 0 ) {
				return result.getFileLocation().substring(result.getQuery().getBaseDir().length());
			}
			if( result.getChildren().length == 1 ) {
				element = result.getChildren()[0];
			}
		}
		
		if( element instanceof XPathResultNode ) {
			XPathResultNode element2 = (XPathResultNode)element;
			if( columnIndex == 0 ) return Messages.DescriptorXPathMatch + element2.getIndex();
			if( columnIndex == 1 ) return element2.getText().trim();
		}
		
		return null; 
	}

	public XPathResultNode[] getResultNodes(XPathQuery query) {
		ArrayList<XPathResultNode> l = new ArrayList<XPathResultNode>();
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			l.addAll(Arrays.asList(files[i].getChildren()));
		}
		return l.toArray(new XPathResultNode[l.size()]);
	}

}
