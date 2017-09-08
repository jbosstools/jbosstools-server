package org.jboss.ide.eclipse.as.reddeer.server.wizard.page;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardPage;
import org.eclipse.reddeer.swt.impl.text.LabeledText;

/**
 * 
 * Adds error check to {@link NewServerWizardPage}
 * 
 * @author psrna
 * @author Radoslav Rabara
 * 
 */

public class NewServerWizardPageWithErrorCheck extends NewServerWizardPage {

	public NewServerWizardPageWithErrorCheck(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	protected final static Logger log = Logger.getLogger(NewServerWizardPageWithErrorCheck.class);
	
	public String getServerName() {
		return new LabeledText(referencedComposite, "Server name:").getText();
	}

	public void checkErrors() {
		String errorText = getErrorText();
		if(errorText == null)
			return;
		checkServerName(errorText);
	}

	private String getErrorText() {
		String text;
		try {
			text = new LabeledText(referencedComposite, "Define a New Server").getText();
			log.info("Found error text: " + text);
		} catch(CoreLayerException e) {
			log.info("No error text found.");
			return null;
		}
		return text;
	}
	
	private void checkServerName(String errorText) {
		if(errorText.contains("The server name is already in use. Specify a different name.")) {
			throw new AssertionError("The server name '" + getServerName() + "' is already in use.");
		}
	}
}

