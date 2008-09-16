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
package org.jboss.ide.eclipse.archives.core.model;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public interface IArchiveAction extends IArchiveNode {

	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.IArchiveAction.";
	public static final String ACTION_TYPE_ATTRIBUTE = ATTRIBUTE_PREFIX + "type";
	public static final String ACTION_TIME_ATTRIBUTE = ATTRIBUTE_PREFIX + "time";

	
	/**
	 * A constant to indicate the action is to run before
	 *  the build 
	 */
	public static final String PRE_BUILD = "preBuild";

	/**
	 * A constant to indicate the action is to run after the build 
	 */
	public static final String POST_BUILD = "postBuild";
	/**
	 * Get whether this should be executed 
	 * before or after building the parent archive.
	 * @return
	 */
	public String getTime();

	/**
	 * Set whether this should be executed 
	 * before or after building the parent archive.
	 * @return
	 */
	public void setTime(String time);

	/**
	 * Get the id of this action's type.
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public String getTypeString();
	
	/**
	 * Get the type of action this is. 
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public IActionType getType();
	
	

	/**
	 * Set the type of action this is. 
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public void setType(String type);
	
	/**
	 * Execute me
	 */
	public void execute();
	
	/**
	 * ToString must give something usable
	 */
	public String toString();
}
