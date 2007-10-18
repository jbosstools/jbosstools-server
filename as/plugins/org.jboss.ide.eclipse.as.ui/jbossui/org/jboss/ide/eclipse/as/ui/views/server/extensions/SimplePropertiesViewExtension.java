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
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.util.Properties;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.ISimplePropertiesHolder;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.SimplePropertiesPropertySheetPage;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public abstract class SimplePropertiesViewExtension 
	extends JBossServerViewExtension implements ISimplePropertiesHolder {

	private SimplePropertiesPropertySheetPage propertiesSheet;
	
	public SimplePropertiesViewExtension() {
	}
	
	
	public abstract void fillContextMenu(Shell shell, IMenuManager menu, Object selection);
	public abstract ITreeContentProvider getContentProvider();
	public abstract LabelProvider getLabelProvider();

	
	public IPropertySheetPage getPropertySheetPage() {
		if( propertiesSheet == null ) {
			propertiesSheet = PropertySheetFactory.createSimplePropertiesSheet(this);
		}
		return propertiesSheet;
	}
	
	public abstract String[] getPropertyKeys(Object selected);
	public abstract Properties getProperties(Object selected);


}
