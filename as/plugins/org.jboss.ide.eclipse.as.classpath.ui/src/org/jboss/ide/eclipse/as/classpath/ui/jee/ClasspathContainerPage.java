/*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.classpath.ui.jee;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.ide.eclipse.as.classpath.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public abstract class ClasspathContainerPage extends WizardPage implements
		IClasspathContainerPage {
	private final static String PAGE_NAME = ClasspathContainerPage.class
			.getName();

	protected String containerId;
	protected String description;
	public ClasspathContainerPage(String id, String description) {
		super(PAGE_NAME);
		this.containerId = id;
		this.description = description;
		this.setTitle(Messages.jeeClasspathAdding + description);
		this.setDescription(Messages.jeeClasspathDescription);
		this.setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY);
	}

	/**
	 * Description of the Method
	 * 
	 * @param parent
	 *            Description of the Parameter
	 */
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		top.setLayout(layout);

		Label lbl = new Label(top, SWT.NONE);
		lbl.setText(Messages.jeeClasspathBody1 + 
				this.getClasspathEntryDescription() + 
				Messages.jeeClasspathBody2);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		lbl.setLayoutData(gd);

		this.setControl(top);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean finish() {
		return true;
	}

	/**
	 * Gets the selection attribute of the ClasspathContainerPage object
	 * 
	 * @return The selection value
	 */
	public IClasspathEntry getSelection() {
		return JavaCore.newContainerEntry(new Path(containerId), true);
	}

	/**
	 * Sets the selection attribute of the ClasspathContainerPage object
	 * 
	 * @param containerEntry
	 *            The new selection value
	 */
	public void setSelection(IClasspathEntry containerEntry) {
	}

	/**
	 * Gets the classpathContainerId attribute of the ClasspathContainerPage
	 * object
	 * 
	 * @return The classpathContainerId value
	 */
	protected String getClasspathContainerId() {
		return containerId;
	}

	/**
	 * Gets the classpathEntryDescription attribute of the
	 * ClasspathContainerPage object
	 * 
	 * @return The classpathEntryDescription value
	 */
	protected String getClasspathEntryDescription() {
		return description;
	}
}
