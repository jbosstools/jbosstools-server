/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.wizard.page;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Combo;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.text.DefaultText;

/**
 * TODO comment here
 * @author Pavol Srna
 *
 */
public class NewServerRSIWizardPage extends WizardPage{
	
	
	public NewServerRSIWizardPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	/**
	 * Set Remote Server Home path
	 * @param path
	 */
	public void setRemoteServerHome(String path){
		new DefaultText(referencedComposite, 0).setText(path);
	}

	/**
	 * @return Remote Server Home text
	 */
	public String getRemoteServerHome() {
		return new DefaultText(referencedComposite, 0).getText();
	}
	
	/**
	 * @return list of all configured hosts
	 */
	public List<String> getHosts() {
		return new LinkedList<String>(getHostCombo().getItems());
	}	
	
	/**
	 * Select host from combo on Remote System Integration wizard page.
	 * @param host to be selected
	 * @return false if host to be selected is not listed in combo
	 */
	public boolean selectHost(String host){
		if(getHosts().contains(host)){
			getHostCombo().setSelection(host);
			return true;
		}
		return false;
	}
	
	private Combo getHostCombo() {
		return new DefaultCombo(referencedComposite, 0);
	}
	
	public RSEMainNewConnectionWizard createNewHost(){
		new PushButton(referencedComposite, "New Host...").click();
		return new RSEMainNewConnectionWizard();
		
	}
}
