/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal.controls;


import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jboss.tools.jmx.ui.extensions.IAttributeControlFactory;
import org.jboss.tools.jmx.ui.extensions.IWritableAttributeHandler;
import org.jboss.tools.jmx.core.util.StringUtils;

public abstract class AbstractTabularControlFactory 
		implements IAttributeControlFactory {

	private void copyPressed(Table table) {
    	StringBuffer sb = new StringBuffer();
    	TableItem[] items = table.getSelection();
    	for( int i = 0; i < items.length; i++ ) {
    		sb.append(items[i].getText());
    		sb.append("\n"); //$NON-NLS-1$
    	}
    	String asString = sb.toString();
    	final Clipboard cb = new Clipboard(table.getDisplay());
    	if (asString.length() > 0) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[]{asString}, new Transfer[]{textTransfer});
		}
	}
	
	public Control createControl(final Composite parent, final FormToolkit toolkit,
			final boolean writable, final String type, final Object value, 
			final IWritableAttributeHandler handler) {

		int style = SWT.MULTI | SWT.FULL_SELECTION;
        Table table = null;
        if (toolkit != null) {
            table = toolkit.createTable(parent, style);
            toolkit.paintBordersFor(parent);
        } else {
            table = new Table(parent, style | SWT.BORDER);
        }

        Menu menu = new Menu(table); // where table1 is your table
        MenuItem item1 = new MenuItem(menu, SWT.PUSH);
        item1.setText("Copy"); //$NON-NLS-1$
        final Table table2 = table;
        Listener popUpListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
            	copyPressed(table2);
            }
        };
        item1.addListener(SWT.Selection, popUpListener);
        table.setMenu(menu);
        
        table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
                if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'c')) {
                	copyPressed(table2);
                } else if(((e.stateMask & SWT.COMMAND) == SWT.COMMAND) && (e.keyCode == 'c')) {
                	copyPressed(table2);
                }
			}
		});

        
        
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        table.setLayoutData(gd);
        
        if (value == null) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setWidth(200);
            column.setMoveable(false);
            column.setResizable(true);
            
            table.setHeaderVisible(false);
            table.setLinesVisible(getVisibleLines());
            
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(StringUtils.NULL);
            
        } else {
            fillTable(table, value);
            table.setHeaderVisible(getVisibleHeader());
            table.setLinesVisible(getVisibleLines());
        }
        
        return table;
	}

	protected abstract void fillTable(Table table, Object value);
	
	protected abstract boolean getVisibleHeader();
	
	protected abstract boolean getVisibleLines();
}
