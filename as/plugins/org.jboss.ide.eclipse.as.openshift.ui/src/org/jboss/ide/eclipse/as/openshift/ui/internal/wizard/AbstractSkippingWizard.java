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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;

/**
 * @author André Dietisheim
 */
public abstract class AbstractSkippingWizard extends Wizard implements INewWizard {

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = null;
		while ((nextPage = super.getNextPage(page)) != null) {
			if (ISkipableWizardPage.class.isAssignableFrom(nextPage.getClass())
					&& ((ISkipableWizardPage) nextPage).isSkip()) {
				page = nextPage;
				continue;
			} else {
				break;
			}
		}
		return nextPage;
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage previousPage = null;
		while ((previousPage = super.getPreviousPage(page)) != null) {
			if (ISkipableWizardPage.class.isAssignableFrom(previousPage.getClass())
					&& ((ISkipableWizardPage) previousPage).isSkip()) {
				page = previousPage;
				continue;
			} else {
				break;
			}
		}
		return previousPage;
	}
}
