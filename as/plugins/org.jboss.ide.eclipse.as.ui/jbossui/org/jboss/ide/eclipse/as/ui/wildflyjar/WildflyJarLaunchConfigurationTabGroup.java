/******************************************************************************* 
 * Copyright (c) 2021 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.wildflyjar;

import java.util.Arrays;

import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.ui.internal.launch.MavenJRETab;
import org.eclipse.m2e.ui.internal.launch.MavenLaunchMainTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WildflyJarLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public WildflyJarLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		CustomMavenLaunchMainTab main = new CustomMavenLaunchMainTab();
		setTabs(new WildflyJarLaunchConfigurationTab(main), main, new MavenJRETab(), new RefreshTab(),
				new SourceLookupTab(), new EnvironmentTab(), new CommonTab());
	}

	private static class CustomMavenLaunchMainTab extends MavenLaunchMainTab {
		public void createControl(Composite parent) {
			super.createControl(parent);
			goalsText.setEnabled(false);
		}

		public void setGoal(String text) {
			goalsText.setText(text);
		}
	}

	public static class WildflyJarLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
		private Combo combo = null;
		private String[] opts = new String[] { "dev", "dev-watch", "run", "start", "shutdown" };
		private CustomMavenLaunchMainTab main;

		public WildflyJarLaunchConfigurationTab(CustomMavenLaunchMainTab main) {
			this.main = main;
		}

		/*
		 * dev: To build a bootable JAR in 'dev' mode. dev-watch: To launch the bootable
		 * JAR foreground (blocking). run: To launch the bootable JAR foreground
		 * (blocking). start: To launch the bootable JAR in background (non blocking).
		 * shutdown: To kill a running bootable JAR.
		 */

		@Override
		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout(2, false));
			container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label l = new Label(container, SWT.NONE);
			l.setText("wildfly-jar goal");

			combo = new Combo(container, SWT.READ_ONLY);
			
			combo.setItems(opts);
			combo.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int sel = combo.getSelectionIndex();
					if (sel == -1) {
						main.setGoal("");
					} else {
						main.setGoal("wildfly-jar:" + combo.getItem(sel));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			setControl(container);
		}

		@Override
		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
			IJavaElement javaElement = getContext();
			if (javaElement != null && isWildflyJarProject(javaElement.getJavaProject())) {
				// TODO set the defaults
			} else {
				// Set defaults with no known information
			}
		}

		protected boolean isWildflyJarProject(IJavaProject jp) {
			return WildflyJarPropertyTester.isWildflyJarProject(jp.getProject());
		}

		public static IWorkbenchPage getActivePage() {
			IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (w != null) {
				return w.getActivePage();
			}
			return null;
		}

		protected IJavaElement getContext() {
			IWorkbenchPage page = getActivePage();
			if (page != null) {
				ISelection selection = page.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					if (!ss.isEmpty()) {
						Object obj = ss.getFirstElement();
						if (obj instanceof IJavaElement) {
							return (IJavaElement) obj;
						}
						if (obj instanceof IResource) {
							IJavaElement je = JavaCore.create((IResource) obj);
							if (je == null) {
								IProject pro = ((IResource) obj).getProject();
								je = JavaCore.create(pro);
							}
							if (je != null) {
								return je;
							}
						}
					}
				}
				IEditorPart part = page.getActiveEditor();
				if (part != null) {
					IEditorInput input = part.getEditorInput();
					return input.getAdapter(IJavaElement.class);
				}
			}
			return null;
		}

		@Override
		public void initializeFrom(ILaunchConfiguration configuration) {
			String goal = getAttribute(configuration, MavenLaunchConstants.ATTR_GOALS, ""); //$NON-NLS-1$
			if( goal != null ) {
				if( goal.startsWith("wildfly-jar:")) {
					goal = goal.substring("wildfly-jar:".length());
				}
				int ind = Arrays.asList(opts).indexOf(goal);
				combo.deselectAll();
				if( ind != -1 ) {
					combo.select(ind);
				} 
			}
		}

		private String getAttribute(ILaunchConfiguration configuration, String name, String defaultValue) {
			try {
				return configuration.getAttribute(name, defaultValue);
			} catch (CoreException ex) {
				return defaultValue;
			}
		}

		@Override
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			// We do nothing here. Woohoo
			main.performApply(configuration);
		}

		@Override
		public String getName() {
			return "Wildfly-jar";
		}

	}
}
