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
public interface IArchivesLogger {
    /** Message priority of &quot;error&quot;. */
    public static final int MSG_ERR = 0;
    /** Message priority of &quot;warning&quot;. */
    public static final int MSG_WARN = 1;
    /** Message priority of &quot;information&quot;. */
    public static final int MSG_INFO = 2;
    /** Message priority of &quot;verbose&quot;. */
    public static final int MSG_VERBOSE = 3;
    /** Message priority of &quot;debug&quot;. */
    public static final int MSG_DEBUG = 4;

    /**
     * Log a message
     * @param severety
     * @param message
     * @param error
     */
	public void log(int severety, String message,Throwable throwable);
}
