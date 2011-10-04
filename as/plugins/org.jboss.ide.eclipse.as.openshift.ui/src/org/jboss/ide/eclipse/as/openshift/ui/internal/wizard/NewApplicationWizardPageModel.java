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
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.common.ui.preferencevalue.StringPreferenceValue;

/**
 * @author André Dietisheim
 */
public class NewApplicationWizardPageModel extends ObservableUIPojo {

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CARTRIDGES = "cartridges";
	public static final String PROPERTY_SELECTED_CARTRIDGE = "selectedCartridge";

	private IUser user;
	private String name;
	
	private Collection<ICartridge> cartridges;
	private String selectedCartridge;
	private StringPreferenceValue selectedCartridgePreference;

	public NewApplicationWizardPageModel(IUser user) {
		this.user = user;
		this.selectedCartridgePreference = new StringPreferenceValue(
				"org.jboss.ide.eclipse.as.openshift.ui.internal.wizard.NewApplicationWizard.selectedCartridge", OpenshiftUIActivator.PLUGIN_ID);
		initSelectedCartridge();
	}

	private void initSelectedCartridge() {
		String selectedCartridge = selectedCartridgePreference.get();
		if (selectedCartridge == null
				|| selectedCartridge.length() == 0) {
			selectedCartridge = ICartridge.JBOSSAS_7.getName();
		}
		this.selectedCartridge = selectedCartridge;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROPERTY_NAME, this.name, this.name = name);
	}

	public void loadCartridges() throws OpenshiftException {
		setCartridges(user.getCartridges());
	}

	public void setCartridges(Collection<ICartridge> cartridges) {
		firePropertyChange(PROPERTY_CARTRIDGES, this.cartridges, this.cartridges = cartridges);
	}

	public Collection<ICartridge> getCartridges() {
		return cartridges;
	}

	public String getSelectedCartridge() {
		return selectedCartridge;
	}

	public void setSelectedCartridge(String name) {
		selectedCartridgePreference.store(name);
		firePropertyChange(PROPERTY_SELECTED_CARTRIDGE, selectedCartridge, this.selectedCartridge = name);
	}

	public void createApplication() throws OpenshiftException {
		user.createApplication(name, user.getCartridgeByName(selectedCartridge));
	}

	public boolean hasApplication(String name) {
		try {
			return user.getApplicationByName(name) != null;
		} catch (OpenshiftException e) {
			// TODO proper logging
			return false;
		}
	}
	
	
}
