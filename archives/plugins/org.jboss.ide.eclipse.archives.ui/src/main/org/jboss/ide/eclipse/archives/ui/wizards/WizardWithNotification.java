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

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class WizardWithNotification extends Wizard implements IPageChangedListener {
	public static final int NEXT = 1;
	public static final int PREVIOUS = 2;
	public static final int FINISH = 3;
	public static final int UNKNOWN = 4;


	private IWizardPage currentPage;
	public WizardWithNotification() {
		super();
		currentPage = null;
	}
    public void setContainer(IWizardContainer wizardContainer) {
    	IWizardContainer previous = getContainer();
    	super.setContainer(wizardContainer);

    	// listeners
    	if( previous instanceof WizardDialog ) {
    		((WizardDialog)previous).removePageChangedListener(this);
    	}

    	if( wizardContainer instanceof WizardDialog ) {
    		((WizardDialog)wizardContainer).addPageChangedListener(this);
    	}
    }
	public void pageChanged(PageChangedEvent event) {
		if( currentPage == null ) {
			currentPage = (IWizardPage)event.getSelectedPage();
			if( currentPage instanceof WizardPageWithNotification) {
				((WizardPageWithNotification)currentPage).pageEntered(UNKNOWN);
			}
			return;
		}

		Object selectedPage = event.getSelectedPage();
		IWizardPage previous = currentPage.getPreviousPage();
		IWizardPage next = currentPage.getNextPage();

		if( previous != null && previous.equals(selectedPage)) {
			if( currentPage instanceof WizardPageWithNotification )
				((WizardPageWithNotification)currentPage).pageExited(PREVIOUS);
			if( selectedPage instanceof WizardPageWithNotification )
				((WizardPageWithNotification)selectedPage).pageEntered(PREVIOUS);
		} else if( next != null && next.equals(selectedPage)) {
			if( currentPage instanceof WizardPageWithNotification )
				((WizardPageWithNotification)currentPage).pageExited(NEXT);
			if( selectedPage instanceof WizardPageWithNotification )
				((WizardPageWithNotification)selectedPage).pageEntered(NEXT);
		} else {
			if( currentPage instanceof WizardPageWithNotification ) {
				((WizardPageWithNotification)currentPage).pageExited(UNKNOWN);
			}
			if( selectedPage instanceof WizardPageWithNotification) {
				((WizardPageWithNotification)selectedPage).pageEntered(UNKNOWN);
			}
		}
		currentPage = selectedPage instanceof IWizardPage ? ((IWizardPage)selectedPage) : null;
	}


}
