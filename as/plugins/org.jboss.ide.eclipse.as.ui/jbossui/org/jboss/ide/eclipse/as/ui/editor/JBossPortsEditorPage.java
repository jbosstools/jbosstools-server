/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;

public class JBossPortsEditorPage extends ServerEditorPart
{

	private JBossServer getJBossServer ()
	{
		IServerWorkingCopy wc = getServer();
		
		return	(JBossServer) wc.getOriginal().loadAdapter(ServerDelegate.class, null);
	}
	
	private Composite createCompositeForSection (FormToolkit toolkit, Section section)
	{
		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		
		return composite;
	}
	
	private Section createSection (FormToolkit toolkit, ScrolledForm form, String text, String description)
	{
		Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
			| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		
		section.setText(text);
		section.setDescription(description);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return section;
	}
	
	public void createPartControl(Composite parent)
	{
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		
		Section section = createSection(toolkit, form, "Ports", "Various ports used by JBoss Application Server");
		Composite composite = createCompositeForSection(toolkit, section);
		
		Label jndiPortLabel = toolkit.createLabel(composite, "JNDI Port:");
		Text jndiPortText = toolkit.createText(composite, "");
		
		form.setContent(section);
		form.reflow(true);
	}
	public void setFocus()
	{
		// ignore
	}
}