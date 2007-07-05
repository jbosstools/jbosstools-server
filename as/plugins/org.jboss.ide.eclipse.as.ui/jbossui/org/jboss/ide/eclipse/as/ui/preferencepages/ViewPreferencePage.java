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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.part.PageBook;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

public class ViewPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String ENABLED = "_ENABLED_";
	
	private Table table;
	
	private Label mainDescriptionLabel, preferenceCompositeDescriptionLabel;
	
	private HashMap providerToGroup;
	private ArrayList preferenceComposites, enabledButtons;
	private PageBook book;
	private Composite mainComposite;
	private ServerViewProvider[] providers;
	
	private Button moveUp, moveDown;
	
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
    		provider.setWeight(i);
    	}
    	
    	// now let the composites save their preferences
		ViewProviderPreferenceComposite comp;
		boolean retval = true;
		for( int i = 0; i < preferenceComposites.size(); i++ ) {
			comp = (ViewProviderPreferenceComposite)preferenceComposites.get(i);
			retval = retval && comp.performOk();
		}
    	
		// refresh the viewer
		JBossServerView.getDefault().refreshAll();
		
        return retval;
    }


    public void dispose() {
    	super.dispose();
		ViewProviderPreferenceComposite comp;
		for( int i = 0; i < preferenceComposites.size(); i++ ) {
			comp = (ViewProviderPreferenceComposite)preferenceComposites.get(i);
			comp.dispose();
		}
    }
    
    
	protected Control createContents(Composite p) {
		providerToGroup = new HashMap();
		mainComposite = new Composite(p, SWT.NONE); 
		mainComposite.setLayout(new FormLayout());
		
	    addEnablementComposite(mainComposite);
	    addExtensionPreferencePages(mainComposite);
	    addListeners();
		
		return mainComposite;
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
		mainDescriptionLabel = new Label(parent, SWT.WRAP);
        FormData labelData = new FormData();
        labelData.left = new FormAttachment(0,5);
        labelData.right = new FormAttachment(100, -5);
        labelData.top = new FormAttachment(0,5);
		mainDescriptionLabel.setLayoutData(labelData);

	//	mainDescriptionLabel.setText(Messages.ViewExtensionEnablementDescription);
		
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());
		FormData cData = new FormData();
        cData.left = new FormAttachment(0,5);
        cData.right = new FormAttachment(100, -5);
        cData.top = new FormAttachment(mainDescriptionLabel,5);
        cData.bottom = new FormAttachment(40,0);
		c.setLayoutData(cData);
		
        table = new Table(c, SWT.BORDER);
        
        FormData tableData = new FormData();
        tableData.left = new FormAttachment(10,5);
        tableData.right = new FormAttachment(100, -5);
        tableData.top = new FormAttachment(0,5);
        tableData.bottom = new FormAttachment(100,0);
        table.setLayoutData(tableData);
        
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setText(Messages.ViewPreferencePageName);
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(Messages.ViewPreferencePageEnabled);
//        TableColumn column2 = new TableColumn(table, SWT.NONE);
//        column2.setText(Messages.ViewPreferencePageWeight);
        TableColumn column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Messages.ViewPreferencePageDescription);
        
        providers = JBossServerUIPlugin.getDefault().getAllServerViewProviders();
        int minWidth = 0;
        
        enabledButtons = new ArrayList();
        for (int i = 0; i < providers.length; i++) {
                final TableItem item = new TableItem(table, SWT.NONE);
                
                // keep the object in the data
                item.setData(providers[i]);
                
                
                item.setText(new String[] {providers[i].getName(), "", providers[i].getDescription()});
                item.setImage(providers[i].getImage());

                final Button b = new Button(table, SWT.CHECK);
                enabledButtons.add(b);
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
        }
        column0.pack();
        column1.pack();
        column1.setWidth(column1.getWidth() + minWidth);
        column3.pack();
        column3.setWidth(column3.getWidth() + minWidth);
        
        
        
        moveUp = new Button(c, SWT.PUSH);
        moveDown = new Button(c, SWT.PUSH);

        moveUp.setText("Move Up");
        moveDown.setText("Move Down");
        
        FormData moveUpData = new FormData();
        moveUpData.left = new FormAttachment(0,0);
        moveUpData.right = new FormAttachment(table, -5);
        moveUpData.top = new FormAttachment(30,0);
        moveUp.setLayoutData(moveUpData);

        FormData moveDownData = new FormData();
        moveDownData.left = new FormAttachment(0,0);
        moveDownData.right = new FormAttachment(table, -5);
        moveDownData.top = new FormAttachment(moveUp, 5);
        moveDown.setLayoutData(moveDownData);

        moveUp.setEnabled(false);
        moveDown.setEnabled(false);
        
        table.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				enableMoveButtons();
			} 
        });
        
        moveUp.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				try {
					move(table.getSelectionIndex(), table.getSelectionIndex()-1);
					enableMoveButtons();
				} catch( Exception ex ) {}
			} 
        });
        moveDown.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				try {
					move(table.getSelectionIndex(), table.getSelectionIndex()+1);
					enableMoveButtons();
				} catch( Exception ex ) {}
			} 
        });
	}
	protected void enableMoveButtons() {
		int index = table.getSelectionIndex();
		int length = table.getItemCount();
		if( index == length-1 && index != 0) {
			moveUp.setEnabled(true);
			moveDown.setEnabled(false);
		} else if( index == 0 && length > 1 ) {
			moveUp.setEnabled(false);
			moveDown.setEnabled(true);
		} else {
			moveUp.setEnabled(true);
			moveDown.setEnabled(true);
		}
	}
	protected void move(int selectedIndex, int swapIndex) {
		TableItem selected = table.getItem(selectedIndex);
		TableItem above = table.getItem(swapIndex);
		
		ServerViewProvider selectedProvider = (ServerViewProvider)selected.getData();
		ServerViewProvider aboveProvider = (ServerViewProvider)above.getData();
		selected.setData(aboveProvider);
		above.setData(selectedProvider);
		
		String[] selectedTemp = new String[] {selected.getText(0), "", selected.getText(2)};
		String[] aboveTemp = new String[] {above.getText(0), "", above.getText(2)};
		selected.setText(aboveTemp);
		above.setText(selectedTemp);
		
		Boolean bigB = ((Boolean)selected.getData(ENABLED));
		selected.setData(ENABLED, above.getData(ENABLED));
		above.setData(ENABLED, bigB);
		
		Image tmp = selected.getImage();
		selected.setImage(above.getImage());
		above.setImage(tmp);
		
		Button selectedButton = (Button)enabledButtons.get(selectedIndex);
		Button nextButton = (Button)enabledButtons.get(swapIndex);
		boolean tmpSelected = selectedButton.getSelection();
		selectedButton.setSelection(nextButton.getSelection());
		nextButton.setSelection(tmpSelected);
		
		table.setSelection(swapIndex);
	}
	protected void addExtensionPreferencePages(Composite parent) {
		
		preferenceCompositeDescriptionLabel = new Label(parent, SWT.WRAP);
        FormData labelData = new FormData();
        labelData.left = new FormAttachment(0,5);
        labelData.right = new FormAttachment(100, -5);
        labelData.top = new FormAttachment(45,5);
        preferenceCompositeDescriptionLabel.setLayoutData(labelData);

		preferenceCompositeDescriptionLabel.setText(Messages.ViewExtensionPreferenceDescription);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		FormData scData = new FormData();
		scData.left = new FormAttachment(0,5);
		scData.top = new FormAttachment(preferenceCompositeDescriptionLabel, 5);
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
				providers[i].getDelegate().createPreferenceComposite(g);
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
			enabled.setText(Messages.ViewPreferencePageEnabled);
			enabled.setSelection(provider.isEnabled());
		}
	}
	
	public void init(IWorkbench workbench) {
	}

}
