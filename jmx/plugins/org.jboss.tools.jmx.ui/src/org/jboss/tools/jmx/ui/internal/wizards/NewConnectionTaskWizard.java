/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jmx.ui.internal.wizards;

import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizard;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;
import org.jboss.tools.jmx.ui.Messages;

public class NewConnectionTaskWizard extends TaskWizard {
	public NewConnectionTaskWizard() {
		this(Messages.DefaultConnectionWizardPage_Title, new JMXConnectionTypeFragment());
	}
	public NewConnectionTaskWizard(String title, WizardFragment rootFragment) {
		super(title, rootFragment);
	}
}
