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
package org.jboss.ide.eclipse.as.ui.preferencepages;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.part.PageBook;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;

public class ViewPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String ENABLED = "_ENABLED_";
	private static final String WEIGHT = "_WEIGHT_";
	
	private Table table;
	private HashMap providerToGroup;
	private ArrayList preferenceComposites;
	private PageBook book;
	ServerViewProvider[] providers;
	
	public ViewPreferencePage() {
		providerToGroup = new HashMap();
		preferenceComposites = new ArrayList();
	}

	public ViewPreferencePage(String title) {
		super(title);
	}

	public ViewPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}
	
	public boolean isValid() {
		ViewProviderPreferenceComposite comp;
		for( int i = 0; i < preferenceComposites.size(); i++ ) {
			comp = (ViewProviderPreferenceComposite)preferenceComposites.get(i);
			if( !comp.isValid()) return false;
		}
		return true;
	}

    public boolean performOk() {

    	// first set whether they're enabled
    	TableItem[] items = table.getItems();
    	for( int i = 0; i < items.length; i++ ) {
    		ServerViewProvider provider = (ServerViewProvider)items[i].getData();
    		Boolean bigB = ((Boolean)items[i].getData(ENABLED));
    		if( bigB != null ) 
    			provider.setEnabled(bigB.booleanValue());
    		Integer weight  = ((Integer)items[i].getData(WEIGHT));
    		if( weight != null ) 
    			provider.setWeight(weight.intValue());
    	}
    	
    	// now let the composites save their preferences
		ViewProviderPreferenceComposite comp;
		boolean retval = true;
		for( int i = 0; i < preferenceComposites.size(); i++ ) {
			comp = (ViewProviderPreferenceComposite)preferenceComposites.get(i);
			retval = retval && comp.performOk();
		}
    	
		// refresh the viewer
		JBossServerView.getDefault().refreshJBTree(null);
    	
        return retval;
    }


	protected Control createContents(Composite p) {
		providerToGroup = new HashMap();
		Composite c = new Composite(p, SWT.NONE);
		c.setLayout(new FormLayout());
		
	    addEnablementComposite(c);
	    addExtensionPreferencePages(c);
	    addListeners();
		
		return c;
	}
	
	protected void addListeners() {
		table.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					TableItem item = (table.getSelection())[0];
					Object o = item.getData();
					if( o != null && o instanceof ServerViewProvider) {
						Group g = (Group)providerToGroup.get(((ServerViewProvider)o));
						book.showPage(g);
					}
				} catch( Exception ex ) {
					
				}
			} 
			
		});
	}

	protected void addEnablementComposite(Composite parent) {
		        table = new Table(parent, SWT.BORDER);
		        
		        FormData tableData = new FormData();
		        tableData.left = new FormAttachment(0,5);
		        tableData.right = new FormAttachment(100, -5);
		        tableData.top = new FormAttachment(0,5);
		        tableData.bottom = new FormAttachment(40,0);
		        table.setLayoutData(tableData);
		        
		        table.setHeaderVisible(true);
		        table.setLinesVisible(false);
		        TableColumn column0 = new TableColumn(table, SWT.NONE);
		        column0.setText("Name");
		        TableColumn column1 = new TableColumn(table, SWT.NONE);
		        column1.setText("Enabled");
		        TableColumn column2 = new TableColumn(table, SWT.NONE);
		        column2.setText("Weight");
		        TableColumn column3 = new TableColumn(table, SWT.NONE);
		        column3.setText("Description");
		        
		        providers = JBossServerUIPlugin.getDefault().getAllServerViewProviders();
		        int minWidth = 0;
		        
		        for (int i = 0; i < providers.length; i++) {
		                final TableItem item = new TableItem(table, SWT.NONE);
		                
		                // keep the object in the data
		                item.setData(providers[i]);
		                
		                
		                item.setText(new String[] {providers[i].getName(), "", "", providers[i].getDescription()});
		                item.setImage(providers[i].getImage());

                        final Button b = new Button(table, SWT.CHECK);
                        b.setSelection(providers[i].isEnabled());
                        b.pack();
                        TableEditor editor = new TableEditor(table);
                        Point size = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        editor.minimumWidth = size.x;
                        minWidth = Math.max(size.x, minWidth);
                        editor.minimumHeight = size.y;
                        editor.horizontalAlignment = SWT.CENTER;
                        editor.verticalAlignment = SWT.CENTER;
                        editor.setEditor(b, item , 1);
                        b.addSelectionListener(new SelectionListener() {
							public void widgetDefaultSelected(SelectionEvent e) {
							}

							public void widgetSelected(SelectionEvent e) {
								item.setData(ENABLED, new Boolean(b.getSelection()));
							} 
                        });
                        
                        TableEditor editor2 = new TableEditor(table);

                        final Spinner s = new Spinner(table, SWT.NONE);
                        s.setMaximum(50);
                        s.setMinimum(0);
                        s.setIncrement(1);
                        s.setSelection(providers[i].getWeight());
                        Point size2 = s.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        editor2.minimumWidth = size2.x;
                        minWidth = Math.max(size2.x, minWidth);
                        editor2.minimumHeight = size2.y;
                        editor2.horizontalAlignment = SWT.RIGHT;
                        editor2.verticalAlignment = SWT.CENTER;
                        editor2.setEditor(s, item , 2);
                        
                        s.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent e) {
								item.setData(WEIGHT, new Integer(s.getSelection()));
							} 
                        });
                        
                        
		        }
		        column0.pack();
		        column1.pack();
		        column1.setWidth(column1.getWidth() + minWidth);
		        column2.pack();
		        column2.setWidth(column2.getWidth() + minWidth);
		        column3.pack();
		        column3.setWidth(column3.getWidth() + minWidth);
		        
		
	}
	
	protected void addExtensionPreferencePages(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		FormData scData = new FormData();
		scData.left = new FormAttachment(0,5);
		scData.top = new FormAttachment(45, 0);
		scData.right = new FormAttachment(100, -5);
		scData.bottom = new FormAttachment(100, -5);
		sc.setLayoutData(scData);
		
		
		Composite sub = new Composite(sc, SWT.NONE);
	    sc.setContent(sub);
		sub.setLayout(new FillLayout());
		
		book = new PageBook(sub, SWT.NONE);
		
	    
	    
		ServerViewProvider[] providers = JBossServerUIPlugin.getDefault().getAllServerViewProviders();
		
		for( int i = 0; i < providers.length; i++ ) {
			Group g = new Group(book, SWT.NONE);
			g.setText(providers[i].getName() + " Preferences");
			g.setLayout(new FillLayout());
			ViewProviderPreferenceComposite c = 
				providers[i].getDelegate().createPreferenceComposite(g, SWT.NONE);
			if( c != null ) preferenceComposites.add(c);
			providerToGroup.put(providers[i], g);
		}
		
		
		
		// force a layout
		sub.pack();
		//sub.layout();
		
        int locY = sub.getLocation().y;
        int locX = sub.getLocation().x;
        int sY = sub.getSize().y;
        int sX = sub.getSize().x;

		
	    sc.setExpandHorizontal(true);
	    sc.setExpandVertical(true);
	    sc.setMinHeight(locY + sY);
	    sc.setMinWidth(locX + sX);
	}

	protected class EnablementComposite extends Composite {
		private ServerViewProvider myProvider;
		public EnablementComposite(Composite parent, ServerViewProvider provider ) {
			super(parent, SWT.NONE);
			myProvider = provider;
			
			setLayout(new FormLayout());
			Button enabled = new Button(this, SWT.CHECK);
			enabled.setText("Enabled");
			enabled.setSelection(provider.isEnabled());
			
			
			
		}
		
	}
	
	
	
	public void init(IWorkbench workbench) {
	}
	
	
	
	

}
