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
package org.jboss.ide.eclipse.as.openshift.ui.internal.wizard;

import java.util.Collection;
import java.util.Collections;

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * @author André Dietisheim
 * 
 */
public class ApplicationWizardPageModel extends ObservableUIPojo {

	public static final String PROPERTY_SELECTED_APPLICATION = "selectedApplication";

	private IApplication selectedApplication;
	private ServerAdapterWizardModel wizardModel;

	public ApplicationWizardPageModel(ServerAdapterWizardModel wizardModel) {
		this.wizardModel = wizardModel;
	}

	public Collection<IApplication> getApplications() throws OpenshiftException {
		IUser user = wizardModel.getUser();
		if (user == null) {
			return Collections.emptyList();
		}
		return user.getApplications();
	}

	public IApplication getSelectedApplication() {
		return selectedApplication;
	}

	public void setSelectedApplication(IApplication application) {
		firePropertyChange(PROPERTY_SELECTED_APPLICATION, this.selectedApplication, this.selectedApplication = application);
	}

	public void destroyCurrentApplication() throws OpenshiftException {
		if (selectedApplication == null) {
			return;
		}

		selectedApplication.destroy();
	}
	
	public IUser getUser() {
		return wizardModel.getUser();
	}

}
