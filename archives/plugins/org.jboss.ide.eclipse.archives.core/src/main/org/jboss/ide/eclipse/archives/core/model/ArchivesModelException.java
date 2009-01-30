/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
 package org.jboss.ide.eclipse.archives.core.model;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class ArchivesModelException extends RuntimeException {
	private Exception parent;

	public ArchivesModelException(String message) {
		super(message);
	}
	
	public ArchivesModelException(Exception e) {
		super();
		parent = e;
	}
	public Exception getException() {
		return parent;
	}
	public String getMessage() {
		return super.getMessage() != null ? super.getMessage() :   
				parent.getCause() == null ? parent.getMessage() : 
					parent.getCause().getMessage();
	}
	public Throwable getCause() {
		return parent;
	}

}
