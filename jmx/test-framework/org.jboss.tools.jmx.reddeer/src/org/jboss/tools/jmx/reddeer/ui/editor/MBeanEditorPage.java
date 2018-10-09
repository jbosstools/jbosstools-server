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

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;

/**
 * 
 * @author odockal
 *
 */
public abstract class MBeanEditorPage {
	
	@SuppressWarnings("unused")
	private ReferencedComposite composite;
	
	public MBeanEditorPage(ReferencedComposite composite) {
		this.composite = composite;
	}
	
	public Table getTable() {
		getAllSection();
		return new DefaultTable();
	}
	
	public TableItem selectTableItem(String name) {
		TableItem item = getTable().getItem(name, getProperIndex());
		item.select();
		return item;
	}
	
	public boolean containsTableItem(String name) {
		return getTable().containsItem(name, getProperIndex());
	}
		
	public abstract ReferencedComposite getAllSection();
	
	public abstract ReferencedComposite getDetailsSection();
	
	protected abstract int getProperIndex();
}
