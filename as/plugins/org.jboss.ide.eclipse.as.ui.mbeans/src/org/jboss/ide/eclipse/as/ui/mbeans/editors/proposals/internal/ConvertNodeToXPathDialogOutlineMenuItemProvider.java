/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;
import org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.IServiceXMLOutlineActionProvider;
import org.w3c.dom.Node;

/**
 * This class is also non-functional 
 * pending a better solution
 * @author rob.stryker@jboss.com
 */
public class ConvertNodeToXPathDialogOutlineMenuItemProvider implements
		IServiceXMLOutlineActionProvider {
	public void release() {
	}

	public void menuAboutToShow(IMenuManager manager, ISelection selection) {
		Object o = ((IStructuredSelection)selection).getFirstElement();
		Node n;
		String attName = null;
		if( o instanceof Node ) {
			n = (Node)o;
			if( n instanceof AttrImpl) {
				attName = ((AttrImpl)n).getName();
				n = ((AttrImpl)n).getOwnerElement();
			}
			final Node n2 = n;
			final String attName2 = attName;
			Action temp = new Action() { 
				public void run() {
					//new ConvertNodeRunnable(n2, attName2).run();
				}
			};
			temp.setText(Messages.ConvertNodeToXPathDialogOutlineMenuItemProvider_AddToXPathsAction);
			temp.setDescription(Messages.ConvertNodeToXPathDialogOutlineMenuItemProvider_AddToXPathsDescription);
			//manager.add(temp);
		}
	}


}
