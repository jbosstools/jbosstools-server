/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.core.tree;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.JMXActivator;


public class Root extends Node {

	private IConnectionWrapper connection;
	private MBeansNode mbeansNode;

	public Root(IConnectionWrapper connection) {
		super(null);
		this.connection = connection;
		mbeansNode = new MBeansNode(this);
		addChild(mbeansNode);
	}

	@Override
	public String toString() {
		return connection != null ? connection.toString() : "Root"; //$NON-NLS-1$
	}

	public int compareTo(Object o) {
		return 0;
	}

	public IConnectionWrapper getConnection() {
		return connection;
	}

	// NOT API, MAY BE REMOVED
	public boolean containsDomain(String domain) {
		return getDomainNode(domain) != null;
	}

	// NOT API, MAY BE REMOVED
	public DomainNode getDomainNode(String domain) {
		Node[] nodes = mbeansNode.getChildren();
		for (Node node : nodes) {
			if (node instanceof DomainNode) {
				DomainNode domainNode = (DomainNode) node;
				if (domain.equals(domainNode.getDomain())) {
					return domainNode;
				}
			}
		}
		return null;
	}

	// NOT API, MAY BE REMOVED
	public void refresh() {
		IConnectionWrapper wrapper = connection;
		if (wrapper != null) {
			try {
				wrapper.disconnect();
				wrapper.connect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public MBeansNode getMBeansNode() {
		return mbeansNode;
	}

	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	public void connect() {
		if (connection != null) {
			try {
				connection.connect();
			} catch (Exception e) {
				IStatus status = new Status(IStatus.WARNING, JMXActivator.PLUGIN_ID, "Failed to connect to " + connection + ". " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
				JMXActivator.getDefault().getLog().log(status);
			}
		}
	}


}
