/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.ui.IPreferenceKeys;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.wizards.composite.JBossRuntimeHomeComposite;
import org.jboss.ide.eclipse.as.ui.wizards.composite.JBossJREComposite;
import org.jboss.ide.eclipse.as.wtp.ui.composites.AbstractJREComposite;
import org.jboss.ide.eclipse.as.wtp.ui.wizard.RuntimeWizardFragment;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Stryker
 */
public abstract class AbstractJBTRuntimeWizardFragment extends RuntimeWizardFragment {

}
