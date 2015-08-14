/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards.composite;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;
import org.jboss.ide.eclipse.as.wtp.ui.composites.AbstractJREComposite;

public class JBossJREComposite extends AbstractJREComposite {
	
	public JBossJREComposite(Composite parent, int style, TaskModel tm) {
		super(parent, style, tm);
	}

	/*
	 * Below are methods that a subclass may override if they have
	 * their own way of getting access to a list of vm's etc. 
	 */

	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( r == null ) {
			r = (IRuntime) getTaskModel().getObject(ServerProfileWizardFragment.TASK_CUSTOM_RUNTIME);
		}
		return r;
	}

	protected boolean isUsingDefaultJRE() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.isUsingDefaultJRE();
	}
	
	protected IVMInstall getStoredJRE() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getHardVM();
	}

	public List<IVMInstall> getValidJREs() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return Arrays.asList(jbsrt.getValidJREs(r.getRuntimeType()));
	}

	@Override
	public IExecutionEnvironment getMinimumExecutionEnvironment() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getMinimumExecutionEnvironment(r.getRuntimeType());
	}

	@Override
	public IExecutionEnvironment getMaximumExecutionEnvironment() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getMaximumExecutionEnvironment(r.getRuntimeType());
	}

	@Override
	public IExecutionEnvironment getStoredExecutionEnvironment() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getExecutionEnvironment();
	}
}
