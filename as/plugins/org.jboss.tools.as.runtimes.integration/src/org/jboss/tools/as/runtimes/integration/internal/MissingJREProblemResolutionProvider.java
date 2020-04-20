/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.debug.ui.jres.AddVMInstallWizard;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.runtime.core.model.IRuntimeDetectionResolution;
import org.jboss.tools.runtime.core.model.IRuntimeDetectionResolutionProvider;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;
import org.jboss.tools.runtime.core.model.RuntimeDetectionProblem;

public class MissingJREProblemResolutionProvider implements IRuntimeDetectionResolutionProvider {
	public static final String MIN_EXEC_ENV = "MIN_EXEC_ENV";
	public static final String MAX_EXEC_ENV = "MAX_EXEC_ENV";
	public static final int MISSING_JRE_CODE = 1337;
	
	
	public MissingJREProblemResolutionProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IRuntimeDetectionResolution[] getResolutions(final RuntimeDetectionProblem problem, final RuntimeDefinition def) {
		if( problem.getCode() != MISSING_JRE_CODE) {
			return new IRuntimeDetectionResolution[0];
		}
		
		IRuntimeDetectionResolution r1 = new IRuntimeDetectionResolution() {
			public String getLabel() {
				return "Add JRE to Workspace";
			}
			public void run(final RuntimeDetectionProblem problem, final RuntimeDefinition definition) {
				ResolutionWizard wiz = new ResolutionWizard(getWorkspaceJREs(), problem, definition);
				WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), wiz);
				if( wd.open() == Window.OK) {
					VMStandin result = wiz.getResult();
					if( result != null ) {
						result.convertToRealVM();
					}
				}
			}
			
		};
		return new IRuntimeDetectionResolution[] { r1 };
	}
	
	private class ResolutionWizard extends AddVMInstallWizard {
		RuntimeDetectionProblem problem = null;
		RuntimeDefinition definition = null;
		public ResolutionWizard(IVMInstall[] currentInstalls, final RuntimeDetectionProblem problem, final RuntimeDefinition definition) {
			super(currentInstalls);
			this.problem = problem;
			this.definition = definition;
		}

		public void addPages() {
			super.addPages();
			String msg = getDescriptionMessage(problem, definition);
			getStartingPage().setDescription(msg);
			getStartingPage().setTitle("Add a JRE");
		}
		public VMStandin getResult() {
			return super.getResult();
		}
	}
	
	private String getDescriptionMessage(final RuntimeDetectionProblem problem, final RuntimeDefinition definition) {
		String min, max;
		min = max = null;
		
		Object minExecEnv = problem.getProperty(MIN_EXEC_ENV);
		Object maxExecEnv = problem.getProperty(MAX_EXEC_ENV);
		if( minExecEnv != null && minExecEnv instanceof IExecutionEnvironment) {
			min = ((IExecutionEnvironment)minExecEnv).getId();
		}
		if( maxExecEnv != null && maxExecEnv instanceof IExecutionEnvironment) {
			max = ((IExecutionEnvironment)maxExecEnv).getId();
		}
		if( min == null && max == null ) {
			// no idea wtf to do 
			return "Please add a JRE to your workspace.";
		}
		if( min == null ) {
			return "Please add a JRE with maximum version " + max;
		}
		if( max == null ) {
			return "Please add a JRE with minimum version " + min;
		}
		return "Please add a JRE with minimum version " + min + " and maximum version " + max;
	}
	
	protected IVMInstall[] getWorkspaceJREs() {
		// fill with JREs
		List<IVMInstall> standins = new ArrayList<IVMInstall>();
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				standins.add(install);
			}
		}
		return (IVMInstall[]) standins.toArray(new IVMInstall[standins.size()]);
	}
}
