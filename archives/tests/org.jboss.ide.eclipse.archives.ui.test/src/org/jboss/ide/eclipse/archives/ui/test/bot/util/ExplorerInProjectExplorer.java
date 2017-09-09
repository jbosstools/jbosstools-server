/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.test.bot.util;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.jboss.tools.archives.reddeer.archives.ui.ProjectArchivesExplorer;

/**
 * Checks if project with given name has project archives support
 * enabled
 * 
 * @author jjankovi
 *
 */
public class ExplorerInProjectExplorer extends AbstractWaitCondition {

	private String project;
	
	public ExplorerInProjectExplorer(String project) {
		this.project = project;
	}
	
	public boolean test() {
		try {
			new ProjectArchivesExplorer(project);
			return true;
		} catch (Exception exc) {
			return false;
		}
	}

	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
