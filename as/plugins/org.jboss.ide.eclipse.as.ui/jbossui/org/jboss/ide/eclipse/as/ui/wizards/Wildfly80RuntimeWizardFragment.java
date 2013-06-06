/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;

public class Wildfly80RuntimeWizardFragment extends JBoss7RuntimeWizardFragment {
	@Override
	protected String getSystemJarPath() {
		return JBossServerType.WILDFLY80.getSystemJarPath();
	}

}
