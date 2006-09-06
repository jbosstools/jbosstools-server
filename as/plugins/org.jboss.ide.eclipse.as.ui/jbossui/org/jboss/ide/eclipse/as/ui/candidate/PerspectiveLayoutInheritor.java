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
package org.jboss.ide.eclipse.as.ui.candidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.internal.PageLayout;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.jboss.ide.eclipse.as.core.util.ASDebug;


public class PerspectiveLayoutInheritor {
	private static final String PERSPECTIVE_EXTENSION_POINT = "org.eclipse.ui.perspectives";
	private static final String PERSPECTIVE_EXTENSIONS_EXTENSION_POINT = "org.eclipse.ui.perspectiveExtensions";

	
	public static final int ACTION_SHORTCUT = 1;
	public static final int VIEW_SHORTCUT = 2;
	public static final int NEW_WIZARD_SHORTCUT = 4;
	public static final int PERSPECTIVE_SHORTCUT = 8;
	public static final int VIEWS = 16;

	
	
	private IPageLayout layout;
	private PageLayoutDelegator delegator;
	
	public PerspectiveLayoutInheritor(IPageLayout layout) {
		this.layout = layout;
		this.delegator = new PageLayoutDelegator(layout, new DefaultFilterer());
	}
	
	public PerspectiveLayoutInheritor(IPageLayout layout, PageLayoutFilter filter) {
		this.layout = layout;
		this.delegator = new PageLayoutDelegator(layout, filter);
	}
	
	public class DefaultFilterer extends PageLayoutFilter {
		private ArrayList viewsAdded = new ArrayList();
		public boolean acceptView( String id, String perspective ) { 
			if( !viewsAdded.contains(id)) {
				viewsAdded.add(id);
				return true; 
			}
			return false;
		}

	}
	
	public abstract class PageLayoutFilter {
		public boolean acceptView( String id, String perspective ) { return true; }
		

		protected HashMap folders = new HashMap();
		
		public boolean shouldCreateFolder(String id, String perspective ) { 
			return !folders.containsKey(id); 
		}
		public void    folderCreated(String id, IPlaceholderFolderLayout folder) {
			folders.put(id, folder);
		}
		public IPlaceholderFolderLayout getFolder(String id) { 
			return (IPlaceholderFolderLayout)folders.get(id); 
		}
		
		public boolean acceptActionSet( String id ) { return true; }
		public boolean acceptNewShortcut( String id ) { return true; }
		public boolean acceptPerspectiveShortcut( String id ) { return true; }
		public boolean acceptViewShortcut( String id ) { return true; }	
		
	}
	
	private class PageLayoutDelegator implements IPageLayout {

		private IPageLayout delegate;
		private PageLayoutFilter filter;
		private String perspectiveID;
		
		public class FolderLayoutWrapper implements IFolderLayout {
			private IPlaceholderFolderLayout layout;
			public FolderLayoutWrapper(IPlaceholderFolderLayout layout) {
				this.layout = layout;
			}
			public void addView(String viewId) {
				if( filter.acceptView(viewId, perspectiveID)) 
					((IFolderLayout)layout).addView(viewId);
			}

			public void addPlaceholder(String viewId) {
				if( filter.acceptView(viewId, perspectiveID)) 
					layout.addPlaceholder(viewId);
			}
		}
		
		// constructor
		public PageLayoutDelegator( IPageLayout delegate, PageLayoutFilter filter ) {
			this.delegate = delegate;
			this.filter = filter;
		}

		public void setPerspective(String id) {
			this.perspectiveID = id;
		}

		
		// folders... return wrappers so we can still keep track of what views are added
		public IFolderLayout createFolder(String folderId, int relationship, float ratio, String refId) {
			if( filter.shouldCreateFolder(folderId, perspectiveID)) {
				FolderLayoutWrapper wrap = 
				 new FolderLayoutWrapper(delegate.createFolder(folderId, relationship, ratio, refId));
				filter.folderCreated(folderId, wrap);
				return wrap;
			}
			return (IFolderLayout)filter.getFolder(folderId);
		}

		public IPlaceholderFolderLayout createPlaceholderFolder(String folderId, int relationship, float ratio, String refId) {
			if( filter.shouldCreateFolder(folderId, perspectiveID)) {
				FolderLayoutWrapper wrap = 
					new FolderLayoutWrapper(delegate.createPlaceholderFolder(folderId, relationship, ratio, refId));
				filter.folderCreated(folderId, wrap);
				return wrap;
			}
			return filter.getFolder(folderId);
		}

		
		// views
		public void addFastView(String viewId) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addFastView(viewId);
		}

		public void addFastView(String viewId, float ratio) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addFastView(viewId, ratio);
		}

		public void addPlaceholder(String viewId, int relationship, float ratio, String refId) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addPlaceholder(viewId, relationship, ratio, refId);
		}

		public void addStandaloneView(String viewId, boolean showTitle, int relationship, float ratio, String refId) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addStandaloneView(viewId, showTitle, relationship, ratio, refId);
		}

		public void addStandaloneViewPlaceholder(String viewId, int relationship, float ratio, String refId, boolean showTitle) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addStandaloneViewPlaceholder(viewId, relationship, ratio, refId, showTitle);
		}

		public void addView(String viewId, int relationship, float ratio, String refId) {
			if( filter.acceptView(viewId, perspectiveID) )
				delegate.addView(viewId, relationship, ratio, refId);
		}


		// shortcuts
		public void addActionSet(String actionSetId) {
			if( filter.acceptActionSet(actionSetId))
				delegate.addActionSet(actionSetId);
		}
		public void addNewWizardShortcut(String id) {
			if( filter.acceptNewShortcut(id))
				delegate.addNewWizardShortcut(id);
		}

		public void addPerspectiveShortcut(String id) {
			if( filter.acceptPerspectiveShortcut(id))
				delegate.addPerspectiveShortcut(id);
		}
		public void addShowViewShortcut(String id) {
			if( filter.acceptViewShortcut(id))
				delegate.addShowViewShortcut(id);
		}

		public void addShowInPart(String id) {
			delegate.addShowInPart(id);
		}

		// other
		public IPerspectiveDescriptor getDescriptor() {
			return delegate.getDescriptor();
		}

		public String getEditorArea() {
			return delegate.getEditorArea();
		}

		public int getEditorReuseThreshold() {
			return delegate.getEditorReuseThreshold();
		}

		public IViewLayout getViewLayout(String id) {
			return delegate.getViewLayout(id);
		}

		public boolean isEditorAreaVisible() {
			return delegate.isEditorAreaVisible();
		}

		public boolean isFixed() {
			return delegate.isFixed();
		}

		public void setEditorAreaVisible(boolean showEditorArea) {
			delegate.setEditorAreaVisible(showEditorArea);
		}

		public void setEditorReuseThreshold(int openEditors) {
			delegate.setEditorReuseThreshold(openEditors);
		}

		public void setFixed(boolean isFixed) {
			delegate.setFixed(isFixed);
		}
		
		public PageLayout getDelegateAsPageLayout() {
			if( delegate instanceof PageLayout ) {
				return (PageLayout)delegate;
			}
			return null;
		}
	}
	
	
	public void inheritInitialLayout(String perspectiveID ) {
		delegator.setPerspective(perspectiveID);
		IConfigurationElement inheritFrom = findPerspective(perspectiveID);
		if( inheritFrom != null ) {
			inheritInitialLayout(delegator, perspectiveID, inheritFrom);
		}
	}

	
	public void inheritExtensions(String perspectiveID, int extensions ) {
		delegator.setPerspective(perspectiveID);
		IConfigurationElement inheritFrom = findPerspective(perspectiveID);
		if( inheritFrom != null ) {
			inheritPerspectiveExtensions(delegator, perspectiveID, inheritFrom, extensions);
		}
	}
	
	
	// Utility
	private IConfigurationElement findPerspective(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = 
			registry.getConfigurationElementsFor(PERSPECTIVE_EXTENSION_POINT);
		
		IConfigurationElement inheritFrom = null;
		for( int i = 0; i < elements.length; i++ ) {
			if( elements[i].getAttribute("id").equals(id)) {
				inheritFrom = elements[i];
			}
			ASDebug.p(elements[i].getAttribute("id"), this);
		}
		return inheritFrom;
	}
	
	
	
	
	private void inheritInitialLayout(IPageLayout layout, String perspectiveID, IConfigurationElement inheritFrom) {
		try {
			PerspectiveDescriptor descriptor = new PerspectiveDescriptor(perspectiveID, inheritFrom);
	        IPerspectiveFactory factory = null;
	        factory = descriptor.createFactory();

			if (factory != null) {
				factory.createInitialLayout(layout);
			}
		} catch( Exception e ) {}
	}
	
	private void inheritPerspectiveExtensions(IPageLayout layout, String perspectiveID, 
			IConfigurationElement inheritFrom, int extensions) {
		boolean actionSets = (extensions & ACTION_SHORTCUT) == ACTION_SHORTCUT;
		boolean viewShortcut = (extensions & VIEW_SHORTCUT ) == VIEW_SHORTCUT;
		boolean wizardShortcut = (extensions & NEW_WIZARD_SHORTCUT ) == NEW_WIZARD_SHORTCUT;
		boolean perspectiveShortcut = (extensions & PERSPECTIVE_SHORTCUT ) == PERSPECTIVE_SHORTCUT;
		boolean view = (extensions & VIEWS) == VIEWS;
		
		IConfigurationElement[] perspectiveExtensions = findPerspectiveExtensions(perspectiveID);
			if( actionSets ) {
				inheritPerspectiveActionSetExtensions(layout, perspectiveExtensions);
			}
			if( viewShortcut ) {
				inheritPerspectiveViewShortcutExtensions(layout, perspectiveExtensions);
			}
			if( wizardShortcut ) {
				inheritPerspectiveNewWizardExtensions(layout, perspectiveExtensions);
			}
			if( perspectiveShortcut ) {
				inheritPerspectivePerspectiveShortcutExtensions(layout, perspectiveExtensions);
			}
			if( view ) {
				inheritPerspectiveViewExtensions(layout, perspectiveExtensions);
			}
	}

	private void inheritPerspectiveActionSetExtensions(IPageLayout layout, IConfigurationElement[] extensions) {
		for( int i = 0; i < extensions.length; i++ ) {
			IConfigurationElement[] elements = extensions[i].getChildren("actionSet");
			for( int j = 0; j < elements.length; j++ ) {
				layout.addActionSet(elements[j].getAttribute("id"));
			}
		}
	}

	private void inheritPerspectiveViewShortcutExtensions(IPageLayout layout, IConfigurationElement[] extensions) {
		for( int i = 0; i < extensions.length; i++ ) {
			IConfigurationElement[] elements = extensions[i].getChildren("viewShortcut");
			for( int j = 0; j < elements.length; j++ ) {
				String x = elements[j].getAttribute("id");
				layout.addShowViewShortcut(x);
			}
		}
	}

	private void inheritPerspectiveNewWizardExtensions(IPageLayout layout, IConfigurationElement[] extensions) {
		for( int i = 0; i < extensions.length; i++ ) {
			IConfigurationElement[] elements = extensions[i].getChildren("newWizardShortcut");
			for( int j = 0; j < elements.length; j++ ) {
				layout.addNewWizardShortcut(elements[j].getAttribute("id"));
			}
		}
	}

	private void inheritPerspectivePerspectiveShortcutExtensions(IPageLayout layout, IConfigurationElement[] extensions) {
		for( int i = 0; i < extensions.length; i++ ) {
			IConfigurationElement[] elements = extensions[i].getChildren("perspectiveShortcut");
			for( int j = 0; j < elements.length; j++ ) {
				layout.addPerspectiveShortcut(elements[j].getAttribute("id"));
			}
		}
	}

	private void inheritPerspectiveViewExtensions(IPageLayout layout, IConfigurationElement[] extensions) {
		PageLayout pLayout = ((PageLayoutDelegator)layout).getDelegateAsPageLayout();
		Map m = pLayout.getIDtoViewLayoutRecMap();
		
		for( int i = 0; i < extensions.length; i++ ) {
			IConfigurationElement[] elements = extensions[i].getChildren("view");
			for( int j = 0; j < elements.length; j++ ) {
				String id = elements[j].getAttribute("id");
				String relative = elements[j].getAttribute("relative");
				String relationship = elements[j].getAttribute("relationship");
				
				float ratio;
				try {
					ratio = Float.parseFloat(elements[j].getAttribute("ratio"));
				} catch( Exception e ) {
					ratio = -1;
				}
				
				boolean visible = "false".equals(elements[j].getAttribute("visible")) ? false : true;
				boolean closeable = "false".equals(elements[j].getAttribute("closeable")) ? false : true;
				boolean moveable = "false".equals(elements[j].getAttribute("moveable")) ? false : true;
				boolean standalone = "true".equals(elements[j].getAttribute("standalone")) ? true : false;
				boolean showTitle = "false".equals(elements[j].getAttribute("showTitle")) ? false : true;
				boolean fastView = "fast".equals(relationship);
				boolean stackedView = "stack".equals(relationship);
				int relationshipID = IPageLayout.TOP;
				if( !fastView && !stackedView ) {
					if( "left".equals(relationship)) relationshipID = IPageLayout.LEFT;
					if( "right".equals(relationship)) relationshipID = IPageLayout.RIGHT;
					if( "top".equals(relationship)) relationshipID = IPageLayout.TOP;
					if( "bottom".equals(relationship)) relationshipID = IPageLayout.BOTTOM;
				}
				
				
				
				// before we start adding things, first make sure the view isnt already there.
				if( m.containsKey(id)) 
					continue;
				
				
				
				if( fastView ) {
					if( ratio == -1 ) layout.addFastView(id);
					else layout.addFastView(id, ratio);
				} else if( stackedView ){
					if( visible ) {
						pLayout.stackView(id, relative);
					} else {
						pLayout.stackPlaceholder(id, relative);
					}
				} else if( standalone ) {
					if( !visible ) {
						layout.addStandaloneViewPlaceholder(id, relationshipID, ratio, relative, showTitle);
					} else {
						layout.addStandaloneView(id, showTitle, relationshipID, ratio, relative);
					}
				} else {
					if( visible ) {
						layout.addView(id, relationshipID, ratio, relative);
					} else {
						layout.addPlaceholder(id, relationshipID, ratio, relative);
					}
				}
			}
		}
	}

	
	
	
	private IConfigurationElement[] findPerspectiveExtensions(String perspectiveID) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = 
			registry.getConfigurationElementsFor(PERSPECTIVE_EXTENSIONS_EXTENSION_POINT);
		
		ArrayList list = new ArrayList();
		for( int i = 0; i < elements.length; i++ ) {
			if( elements[i].getAttribute("targetID").equals(perspectiveID)) {
				list.add(elements[i]);
			}
		}

		return (IConfigurationElement[]) list.toArray(new IConfigurationElement[list.size()]);
	}


}

