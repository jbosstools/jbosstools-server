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

import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * @author André Dietisheim
 */
public class NewApplicationWizardPageModel extends ObservableUIPojo {

	private static final String PROPERTY_CARTRIDGES = "cartridges";
	
	private IUser user;
	private Collection<ICartridge> cartridges;
	
	public NewApplicationWizardPageModel(IUser user) {
		this.user = user;
	}

	public void loadCartridges() throws OpenshiftException {
		setCartridges(user.getCartridges());
	}

	public void setCartridges(Collection<ICartridge> cartridges) {
		firePropertyChange(PROPERTY_CARTRIDGES, this.cartridges, this.cartridges = cartridges);
	}
}
