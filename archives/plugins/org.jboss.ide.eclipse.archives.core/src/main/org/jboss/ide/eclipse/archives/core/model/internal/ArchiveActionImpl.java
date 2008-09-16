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
package org.jboss.ide.eclipse.archives.core.model.internal;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IActionType;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbAction;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class ArchiveActionImpl extends ArchiveNodeImpl implements IArchiveAction {

	private XbAction actionDelegate;
	
	public ArchiveActionImpl() {
		this(new XbAction());
	}
	public ArchiveActionImpl(XbAction delegate) {
		super(delegate);
		this.actionDelegate = delegate;
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return IArchiveNode.TYPE_ARCHIVE_ACTION;
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNode#getRootArchiveRelativePath()
	 */
	public IPath getRootArchiveRelativePath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#getTime()
	 */
	public String getTime() {
		return actionDelegate.getTime();
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#getType()
	 */
	public String getTypeString() {
		return actionDelegate.getType();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#getType()
	 */
	public IActionType getType() {
		return ArchivesCore.getInstance().getExtensionManager().getActionType(getTypeString());
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#setTime(java.lang.String)
	 */
	public void setTime(String time) {
		attributeChanged(ACTION_TIME_ATTRIBUTE, getTime(), time);
		actionDelegate.setType(time);
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#getType(java.lang.String)
	 */
	public void setType(String type) {
		attributeChanged(ACTION_TYPE_ATTRIBUTE, getType(), type);
		actionDelegate.setType(type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#validateChild(org.jboss.ide.eclipse.archives.core.model.IArchiveNode)
	 */
	public boolean validateModel() {
		if( getAllChildren().length != 0 ) return false;
		if( getParent() != null && getModelRootNode() != null && 
			(getParent().getNodeType() != IArchiveNode.TYPE_ARCHIVE || 
				!((IArchive)getParent()).isTopLevel()))
			return false;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveAction#execute()
	 */
	public void execute() {
		getType().execute(this);
	}
	
	public String toString() {
		return getType().getStringRepresentation(this);
	}
}