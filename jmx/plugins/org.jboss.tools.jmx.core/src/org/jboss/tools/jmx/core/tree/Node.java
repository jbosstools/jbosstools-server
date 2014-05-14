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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node implements Comparable, HasRoot {

	protected Node parent;
	private List<Node> children = new ArrayList<Node>();

	public Node(Node parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	public Node addChild(Node node) {
		if (!children.contains(node)) {
			children.add(node);
			Collections.sort(children);
			return node;
		} else {
			return children.get(children.indexOf(node));
		}
	}

	public boolean removeChild(Node child) {
		return children.remove(child);
	}

	public Node[] getChildren() {
		return children.toArray(new Node[children.size()]);
	}

	public List<Node> getChildrenList() {
		return children;
	}

	public Node getParent() {
		return parent;
	}

	public void clearChildren() {
		children.clear();
	}

	public Root getRoot() {
		Node p = parent;
		while(p.getParent() != null) {
			if( p instanceof Root )
				return ((Root)p);
			p = p.getParent();
		}
		return null;
	}
}
