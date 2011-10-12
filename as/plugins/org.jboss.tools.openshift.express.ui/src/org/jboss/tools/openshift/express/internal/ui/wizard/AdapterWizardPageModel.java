/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.wizard;

import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * @author André Dietisheim
 * @author Rob Stryker
 */
public class AdapterWizardPageModel extends ObservableUIPojo {
	public static final String PROPERTY_CLONEDIR = "cloneDir";
	public static final String PROPERTY_BRANCH = "branch";
	
	public static final String CREATE_SERVER = "createServer";
	public static final String MODE = "serverMode";
	public static final String MODE_SOURCE = "serverModeSource";
	public static final String MODE_BINARY = "serverModeBinary";
	public static final String RUNTIME_DELEGATE = "runtimeDelegate";
	public static final String SERVER_TYPE = "serverType";

	private String cloneDir;
	private String branch;
	
	private ImportProjectWizardModel wizardModel;

	public AdapterWizardPageModel(ImportProjectWizardModel wizardModel) {
		this.wizardModel = wizardModel;
	}
	
	public String getCloneDir() {
		return cloneDir;
	}

	public void setCloneDir(String cloneDir) {
		firePropertyChange(PROPERTY_CLONEDIR, cloneDir, this.cloneDir = cloneDir);
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		firePropertyChange(PROPERTY_BRANCH, branch, this.branch = branch);
	}

	// TODO is this the best way? Or should we expose ONLY getters to the parent model?
	public ImportProjectWizardModel getParentModel() {
		return wizardModel;
	}

}
