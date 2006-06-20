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
package org.jboss.ide.eclipse.as.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.view.servers.ServersView;


public class JBossServerView extends ServersView {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight";
	protected int[] rows;
	protected SashForm form;
	protected JBossServerTableViewer jbViewer;
	protected Tree jbTreeTable;
	
	public void createPartControl(Composite parent) {
				
		form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(new FillLayout());
		
		addServerViewer(form);
		addSecondViewer(form);
		
		form.setWeights(rows);
		
	}

	private void addServerViewer(Composite form) {
		Composite child1 = new Composite(form,SWT.NONE);
		child1.setLayout(new GridLayout());
		super.createPartControl(child1);
		//tableViewer.setContentProvider(new ServerContentProvider2());

	}
	
	private void addSecondViewer(Composite form) {
		Composite child2 = new Composite(form,SWT.NONE);
		child2.setLayout(new FillLayout());
		jbTreeTable = new Tree(child2, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		jbViewer = new JBossServerTableViewer(jbTreeTable, tableViewer);
	}
	
	
	
	/**
	 * Save some state
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		int sum = 0;
		rows = new int[2];
		for (int i = 0; i < 2; i++) {
			rows[i] = (i == rows.length-1 ? 100-sum : 50);
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					rows[i] = in.intValue();
			}
			sum += rows[i];
		}
	}

	public void saveState(IMemento memento) {
		int[] weights = form.getWeights();
		for (int i = 0; i < 2; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
	}

}