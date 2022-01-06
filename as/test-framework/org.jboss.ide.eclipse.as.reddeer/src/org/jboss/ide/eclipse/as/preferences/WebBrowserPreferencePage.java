/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.preferences;

import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.button.RadioButton;

/**
 * Class represents General -> Web Browser Preferences page
 * @author odockal
 *
 */
public class WebBrowserPreferencePage extends PreferencePage {

	public WebBrowserPreferencePage(ReferencedComposite referencedComposite, String[] path) {
		super(referencedComposite, path);
	}
	
	public void toggleInternalBrowser() {
		getInternalBrowserCheckBox().toggle(true);
	}
	
	public void toggleExternalBrowser() {
		getExternalBrowserCheckBox().toggle(true);
	}
	
	public RadioButton getInternalBrowserCheckBox() {
		return new RadioButton(new WithTextMatcher("Use &internal web browser"));
	}
	
	public RadioButton getExternalBrowserCheckBox() {
		return new RadioButton(new WithTextMatcher("Use e&xternal web browser"));
	}

}
