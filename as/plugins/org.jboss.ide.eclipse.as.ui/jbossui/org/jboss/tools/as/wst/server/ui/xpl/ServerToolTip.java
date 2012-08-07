/**********************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.jface.internal.text.html.HTML2TextReader;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class ServerToolTip extends ToolTip {	
	protected Shell FOCUSSED_TOOLTIP;
	protected Object tooltipSelection;
	protected Label hintLabel;
	protected Tree tree;
	protected int x;
	protected int y;

	public ServerToolTip(final Tree tree) {
		super(tree);
		
		this.tree = tree;
		
		tree.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				x = e.x;
				y = e.y;
			}
		});
		tree.addKeyListener( new CustomKeyListener(true, true));
	}
	
	// Created a key listener to add it to both the shell and the styledText of the focussed tooltip
	private class CustomKeyListener implements KeyListener {
		private boolean handleEscape, handlef6;
		public CustomKeyListener( boolean handlesEscape, boolean handlesf6) {
			this.handleEscape = handlesEscape; 
			this.handlef6 = handlesf6;
		}
		public void keyPressed(KeyEvent  e) {
			if (e == null)
				return;
			if( handleEscape && e.keyCode == SWT.ESC)
				handleEscape();
			if( handlef6 && e.keyCode == SWT.F6) 
				handlef6();
		}
		public void keyReleased(KeyEvent e) {
		}
	};
	protected void handleEscape() {
		if (FOCUSSED_TOOLTIP != null) {
			FOCUSSED_TOOLTIP.dispose();
			FOCUSSED_TOOLTIP = null;
			tooltipSelection = null;
		}
		activate();
	}
	protected void handlef6() {
		// We have no focussed window, and we DO have a selection to show in focus
		if (FOCUSSED_TOOLTIP == null && tooltipSelection != null) {
			deactivate();
			hide();
			createFocusedTooltip(tree);
		}
	}
	
	public void createFocusedTooltip(final Control control) {
		final Shell stickyTooltip = new Shell(control.getShell(), SWT.ON_TOP | SWT.TOOL
				| SWT.NO_FOCUS);
		stickyTooltip.setLayout(new FillLayout());
		stickyTooltip.setBackground(stickyTooltip.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		stickyTooltip.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				handleEscape();
			}
		});
		control.getDisplay().asyncExec(new Runnable() {
			public void run() {
				Event event = new Event();
				event.x = x;
				event.y = y;
				event.widget = tree;
				
				createToolTipContentArea(event, stickyTooltip);
				stickyTooltip.pack();
				
				stickyTooltip.setLocation(stickyTooltip.getDisplay().getCursorLocation());				
				hintLabel.setText("Press 'ESC' to Hide.");
				stickyTooltip.setVisible(true);
//				Eventually we want to add a listener that checks if
//              the mouseDown event is occurring outside of the bounds of the tooltip
//              if it is, then hide the tooltip
//				addListener(stickyTooltip);
			}
		});
		FOCUSSED_TOOLTIP = stickyTooltip;
	}
	
	@Override
	protected Object getToolTipArea(Event event) {
		Object o = tree.getItem(new Point(event.x,event.y));
		return o;
	}

	protected boolean shouldCreateToolTip(Event event) {
		if (tree.getItem(new Point(event.x, event.y)) == null)
			return false;
		/* JBT:  Added this check */
		Object o = tree.getItem(new Point(event.x, event.y));
		boolean ret;
		if( o instanceof TreeItem && !isMyType(((TreeItem)o).getData()))
			ret = false;
		else
			ret = super.shouldCreateToolTip(event);
		if( !ret )
			tooltipSelection = null;
		return ret;
	}
	
	/*
	 * JBT:  Added a method
	 */
	protected abstract boolean isMyType(Object selected);
	protected abstract void fillStyledText(Composite parent, StyledText sText, Object o);
	
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Object o = tree.getItem(new Point(event.x, event.y));
		if (o == null)
			return null;
		tooltipSelection = o;
		
		FillLayout layout = (FillLayout)parent.getLayout();
		layout.type = SWT.VERTICAL;
		parent.setLayout(layout);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		// set the default text for the tooltip
		StyledText sText = new StyledText(parent, SWT.NONE);
		sText.setEditable(false);
		sText.setBackground(parent.getBackground());
		sText.addKeyListener(new CustomKeyListener(true, false));
		
		fillStyledText(parent, sText, o);
		
		// add the F6 text 
		hintLabel = new Label(parent,SWT.BORDER);
		hintLabel.setAlignment(SWT.RIGHT);
		hintLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		hintLabel.setText("Press 'F6' for Focus.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		final Font font;
		Display display = parent.getDisplay();
		FontData[] fd = parent.getFont().getFontData();
		int size2 = fd.length;
		for (int i = 0; i < size2; i++)
			fd[i].setHeight(7);
		font = new Font(display, fd);
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				font.dispose();
			}
		});
		hintLabel.setFont(font);
		
		parseText(sText.getText(),sText);
		
		return parent;
	}
	
	protected void parseText(String htmlText,StyledText sText) {	
		TextPresentation presentation = new TextPresentation();
		HTML2TextReader reader = new HTML2TextReader(new StringReader(htmlText), presentation);
		String text;
		
		try {
			text = reader.getString();
		} catch (IOException e) {
			text= ""; //$NON-NLS-1$
		}
		
		sText.setText(text);		
		Iterator iter = presentation.getAllStyleRangeIterator();
		while (iter.hasNext()) {
			StyleRange sr = (StyleRange)iter.next();
			sText.setStyleRange(sr);
		}
	}
}