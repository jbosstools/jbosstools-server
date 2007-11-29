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
package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXAttributesWrapper;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXDomain;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXException;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JMXTreeContentProvider implements ITreeContentProvider {
	
	protected JMXViewProvider provider;
	public JMXTreeContentProvider(JMXViewProvider provider) {
		this.provider = provider;
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ServerViewProvider) {
			if (provider.getServer() == null)
				return new Object[] {};
			if (provider.getServer().getServerState() != IServer.STATE_STARTED) {
				JMXModel.getDefault().clearModel(provider.getServer());
				return new Object[] {};
			}
			JMXDomain[] domains = JMXModel.getDefault().getModel(provider.getServer()).getDomains();
			if (domains == null
					&& JMXModel.getDefault().getModel(provider.getServer()).getException() == null) {
				loadChildrenRefreshViewer(parentElement);
				return new Object[] { JMXViewProvider.LOADING };
			} else if (domains == null
					&& JMXModel.getDefault().getModel(provider.getServer()).getException() != null) {
				return new Object[] { JMXModel.getDefault().getModel(provider.getServer()).getException() };
			}
			return domains;
		}
		if (parentElement instanceof JMXDomain) {
			JMXBean[] beans = ((JMXDomain) parentElement).getBeans();
			if (beans == null
					&& ((JMXDomain) parentElement).getException() == null) {
				loadChildrenRefreshViewer(parentElement);
				return new Object[] { JMXViewProvider.LOADING };
			} else if (beans == null
					&& ((JMXDomain) parentElement).getException() != null) {
				return new Object[] { ((JMXDomain) parentElement)
						.getException() };
			}
			return beans;
		}
		if (parentElement instanceof JMXBean) {
			WrappedMBeanOperationInfo[] operations = ((JMXBean) parentElement)
					.getOperations();
			if (operations == null
					&& ((JMXBean) parentElement).getException() == null) {
				loadChildrenRefreshViewer(parentElement);
				return new Object[] { JMXViewProvider.LOADING };
			} else if (operations == null
					&& ((JMXBean) parentElement).getException() != null) {
				return new Object[] { ((JMXBean) parentElement)
						.getException() };
			}
			// add the Attributes element
			ArrayList<Object> list = new ArrayList<Object>();
			list.add(new JMXAttributesWrapper((JMXBean) parentElement));
			list.addAll(Arrays.asList(operations));
			return (Object[]) list.toArray(new Object[list.size()]);
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null; // unused
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ServerViewProvider) {
			if (provider.getServer() == null || 
					provider.getServer().getServerState() != IServer.STATE_STARTED) {
				return false;
			}
			return true;
		}
		if (element instanceof JMXException)
			return false;
		if (element instanceof WrappedMBeanOperationInfo)
			return false;
		if (element instanceof JMXAttributesWrapper)
			return false;
		return true; // always true?
	}

	public Object[] getElements(Object inputElement) {
		return null; // unused here
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != newInput) {
			provider.setServer((IServer) newInput);
		}
	}
	
	protected void loadChildrenRefreshViewer(final Object parent) {
		new Thread() {
			public void run() {
				provider.loadChildren(parent);
				provider.refreshViewerAsync(parent);
			}
		}.start();
	}


}
