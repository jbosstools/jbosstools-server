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
