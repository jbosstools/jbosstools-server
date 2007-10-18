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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class XPathPropertyContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		// we're a leaf
		if( parentElement instanceof XPathResultNode ) {
			return new Object[0];
		}

		// we're a file node (blah.xml) 
		if( parentElement instanceof XPathFileResult ) {
			if( ((XPathFileResult)parentElement).getChildren().length == 1 ) 
				return new Object[0];
			return ((XPathFileResult)parentElement).getChildren();
		}

		// we're the named element (JNDI)
		if( parentElement instanceof XPathQuery) {
			if( countResultNodes((XPathQuery)parentElement) == 1 ) {
				return new Object[0];
			} else {
				return ((XPathQuery)parentElement).getResults();
			}
		}

		// re-creates it from scratch... hrmm
		if( parentElement instanceof ServerViewProvider ) 
			return new Object[] {"ERROR"}; //XPathModel.getDefault().getCategories()
		return new Object[0];
	}
	
	public int countResultNodes(XPathQuery query) {
		int count = 0;
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			count += files[i].getChildren().length;
		}
		return count;
	}

	public Object getParent(Object element) {
		return null;
	}
	
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0 ? true : false;
	}

	public Object[] getElements(Object inputElement) {
		if( inputElement instanceof XPathCategory ) {
			return ((XPathCategory)inputElement).getQueries();
		}
		return new Object[0];
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
