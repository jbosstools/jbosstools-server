/**
 * JBoss, a Division of Red Hat
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
package org.jboss.ide.eclipse.as.ui.views.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossServerView extends ViewPart {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight";
	
	public static JBossServerView instance;
	private static ArrayList<ISelectionChangedListener> serverFrameListeners = new ArrayList<ISelectionChangedListener>();
	private static ArrayList<ISelectionChangedListener> extensionFrameListeners = new ArrayList<ISelectionChangedListener>();

	public static JBossServerView getDefault() {
		return instance;
	}
	
	public static void addServerFrameListener(ISelectionChangedListener listener) {
		if( !serverFrameListeners.contains(listener))
			serverFrameListeners.add(listener);
		if( getDefault() != null )
			getDefault().getServerFrame().getViewer().addSelectionChangedListener(listener);
	}
	public static void addExtensionFrameListener(ISelectionChangedListener listener) {
		if( !extensionFrameListeners.contains(listener))
			extensionFrameListeners.add(listener);
		if( getDefault() != null )
			getDefault().getExtensionFrame().getViewer().addSelectionChangedListener(listener);
	}

	public static void removeServerFrameListener(ISelectionChangedListener listener) {
		if( serverFrameListeners.contains(listener))
			serverFrameListeners.remove(listener);
		if( getDefault() != null )
			getDefault().getServerFrame().getViewer().removeSelectionChangedListener(listener);
	}
	public static void removeExtensionFrameListener(ISelectionChangedListener listener) {
		if( extensionFrameListeners.contains(listener))
			extensionFrameListeners.remove(listener);
		if( getDefault() != null )
			getDefault().getExtensionFrame().getViewer().removeSelectionChangedListener(listener);
	}

	public static interface IServerViewFrame {
		public IAction[] getActionBarActions();
		public int getDefaultSize();
		public void refresh();
	}
	
	public JBossServerView() {
		super();
		instance = this;		
	}

	private SashForm form;
	private int[] sashRows;
	private IMemento memento;
	
	private IServerViewFrame[] frames;
	private ServerFrame serverFrame;
	private ServerExtensionFrame extensionFrame;
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		this.memento = memento;
		int sum = 0;
		sashRows = new int[2];
		for (int i = 0; i < sashRows.length; i++) {
			sashRows[i] = 50;
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					sashRows[i] = in.intValue();
			}
			sum += sashRows[i];
		}
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		int[] weights = form.getWeights();
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
	}

	public void createPartControl(Composite parent) {
		form = new SashForm(parent, SWT.VERTICAL);
		form.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		form.setLayout(new FillLayout());

		
		Composite topWrapper = new Composite(form, SWT.NONE);
		topWrapper.setLayout(new FillLayout());
		serverFrame = new ServerFrame(topWrapper, this);

		Composite bottomWrapper = new Composite(form, SWT.NONE);
		bottomWrapper.setLayout(new FillLayout());
		extensionFrame = new ServerExtensionFrame(bottomWrapper, this);
		
		
		frames = new IServerViewFrame[] { serverFrame, extensionFrame };

		// add toolbar buttons
		IContributionManager cm = getViewSite().getActionBars().getToolBarManager();
		for( int i = 0; i < frames.length; i++ ) {
			IAction[] actions = frames[i].getActionBarActions();
			for (int j = 0; j < actions.length - 1; j++)
				cm.add(actions[j]);
		}
		
		form.setWeights(sashRows);
		
		
		// if the extensions have already been created, add those listeners now.
		Iterator<ISelectionChangedListener> k;
		for(k = serverFrameListeners.iterator(); k.hasNext();) 
			serverFrame.getViewer().addSelectionChangedListener(k.next());
		for(k = extensionFrameListeners.iterator(); k.hasNext();) 
			extensionFrame.getViewer().addSelectionChangedListener(k.next());
	}
	
	public void refreshAll() {
		for( int i = 0; i < frames.length; i++ ) {
			frames[i].refresh();
		}
	}
	
	public ServerFrame getServerFrame() { return this.serverFrame; }
	public ServerExtensionFrame getExtensionFrame() { return this.extensionFrame; }
	
	public void setFocus() {
		getExtensionFrame().getViewer().getTree().setFocus();
	}
	
	public IServer getSelectedServer() {
		return serverFrame == null ? null : serverFrame.getSelectedServer();
	}

	public Object getAdapter(Class adaptor) {
		if( adaptor == IPropertySheetPage.class) {
			return extensionFrame.getViewer().getPropertySheet();
		}
		return super.getAdapter(adaptor);
	}
}
