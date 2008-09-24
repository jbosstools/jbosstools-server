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

package org.jboss.ide.eclipse.archives.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class WizardPageWithNotification extends WizardPage implements IWizardPage {
	/**
	 * @param pageName
	 */
	protected WizardPageWithNotification(String pageName) {
		super(pageName);
	}

    protected WizardPageWithNotification(String pageName, String title,
            ImageDescriptor titleImage) {
    	super(pageName, title, titleImage);
    }


    public void pageEntered(int button) {}
    public void pageExited(int button) {}
}
