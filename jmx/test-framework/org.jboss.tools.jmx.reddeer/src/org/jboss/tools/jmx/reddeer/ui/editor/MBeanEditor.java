/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.reddeer.ui.editor;

import java.util.regex.Pattern;

import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.core.util.InstanceValidator;
import org.eclipse.reddeer.swt.impl.ctab.DefaultCTabItem;
import org.eclipse.reddeer.workbench.core.lookup.EditorPartLookup;
import org.eclipse.reddeer.workbench.handler.EditorHandler;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.eclipse.reddeer.workbench.matcher.EditorPartTitleMatcher;
import org.eclipse.reddeer.workbench.part.AbstractWorkbenchPart;
import org.eclipse.ui.IEditorPart;
import org.hamcrest.Matcher;

public class MBeanEditor extends AbstractWorkbenchPart {
	
	private IEditorPart editorPart;
	
	public IEditorPart getEditorPart() {
		return editorPart;
	}
	
	public MBeanEditor(String title) {
		this(new WithTextMatcher(title));
	}
	
	@SuppressWarnings("unchecked")
	public MBeanEditor(Matcher<String> title) {
		this(new EditorPartTitleMatcher(title));
	}
	
	@SuppressWarnings("unchecked") 
	public MBeanEditor(Matcher<IEditorPart>... matchers) {
		this(EditorPartLookup.getInstance().getEditor(matchers));
	}
	
	protected MBeanEditor(IEditorPart part) {
		super(new DefaultCTabItem(new WorkbenchShell(), new WithTextMatcher(new RegexMatcher("\\*?" + Pattern.quote(part.getTitle())))));
		InstanceValidator.checkNotNull(part, "part");
		this.editorPart = part;
		activate();
	}

	@Override
	public void activate() {
		EditorHandler.getInstance().activate(editorPart);
	}
	
	@Override
	public void close() {
		EditorHandler.getInstance().close(false, editorPart);
	}
	
	/**
	 * Activate the page (tab) with the given name.
	 *
	 * @param name the name
	 */
	public void selectPage(String name) {
		activate();
		new DefaultCTabItem(name).activate();
	}
	
	public void selectAttributesPage() {
		selectPage("Attributes");
	}
	
	public void selectOperationsPage() {
		selectPage("Operations");
	}
	
	public void selectNotificationsPage() {
		selectPage("Notifications");
	}
	
	public void selectInfoPage() {
		selectPage("Info");
	}
	
	public AttributesPage getAttributesPage() {
		selectAttributesPage();
		return new AttributesPage(this);
	}
	
	public OperationsPage getOperationsPage() {
		selectOperationsPage();
		return new OperationsPage(this);
	}
}
