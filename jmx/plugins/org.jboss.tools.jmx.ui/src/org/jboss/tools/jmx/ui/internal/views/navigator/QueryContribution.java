/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.views.navigator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;

public class QueryContribution {
	private static Boolean TRUE_BOOL = new Boolean(true);
	private static HashMap<Viewer, QueryContribution> map = 
			new HashMap<Viewer, QueryContribution>();
	
	public static QueryContribution getContributionFor(Viewer v) {
		return map.get(v);
	}
	
	public static String getFilterText(Viewer viewer) {
		QueryContribution qc = map.get(viewer);
		if( qc != null ) {
			return qc.filterText;
		}
		return null;
	}


	public static class QueryFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			
			QueryContribution contrib = QueryContribution.getContributionFor(viewer);
			if( contrib != null ) {
				return contrib.shouldShow(element, parentElement);
			}
			return true;
		}
	}

	
	private String filterText;
	private JMXNavigator navigator;
	private HashMap<Object, Boolean> matches = null;
	private HashMap<Object, Boolean> shouldShow = null;
	private RefineThread refineThread;
	
	public QueryContribution(final JMXNavigator navigator) {
		this.navigator = navigator;
		map.put(navigator.getCommonViewer(), this);
		addListener();
	}

	protected void addListener() {
		navigator.getFilterText().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean searchNew = matches == null || filterText == null || filterText.equals("")  //$NON-NLS-1$
								|| !navigator.getFilterText().getText().startsWith(filterText);
				RefineThread thread = new RefineThread(searchNew,
						navigator.getFilterText().getText(), 
						matches, shouldShow);
				if( !navigator.getFilterText().getText().equals("")) { //$NON-NLS-1$
					if( refineThread != null )
						refineThread.cancel();
					refineThread = thread;
					refineThread.start();
				} else {
					matches = null;
					shouldShow = null;
					refreshView();
				}
			} 
		});
	}
	
	protected void refreshView() {
		Display.getDefault().asyncExec(new Runnable() { 
			public void run() {
				navigator.getCommonViewer().refresh();
			}
		});
	}
	
	public class RefineThread extends Thread {
		private boolean canceled = false;
		private boolean searchNew;
		private String newFilter;
		private HashMap<Object, Boolean> matchClone;
		private HashMap<Object, Boolean> showClone;
		public RefineThread(
				boolean searchNew, String newFilter,
				HashMap<Object, Boolean> matches,
				HashMap<Object, Boolean> shouldShow) {
			this.searchNew = searchNew;
			this.newFilter = newFilter;
			this.matchClone = matches == null ? 
					new HashMap<Object, Boolean>() :
					(HashMap<Object, Boolean>) matches.clone();
			this.showClone = shouldShow == null ? 
					new HashMap<Object, Boolean>() :
					(HashMap<Object, Boolean>) shouldShow.clone();
		}
		
		
		/*
		 * TODO MAKE SURE YOU FIX THE CONTENT PROVIDER
		 * IT IS NOT RETURNING GETPARENT AS IT SHOULD!!!
		 * 
		 */
		public void run() {
			if( searchNew ) 
				searchNew();
			else
				refine();
			
			if( !canceled ) {
				threadFinished(matchClone, showClone, newFilter);
			}
		}
		
		protected void searchNew() {
			ITreeContentProvider provider = (ITreeContentProvider)navigator.getCommonViewer().getContentProvider();
			Object[] elements = provider.getElements(navigator.getCommonViewer().getInput());
			for( int i = 0; i < elements.length; i++ )
				if( !canceled )
					fullCache(elements[i], provider);
		}
		
		protected void fullCache(Object o, ITreeContentProvider provider) {
			boolean found = false;
			String elementAsString = MBeanExplorerLabelProvider.getText2(o);
			if( elementAsString.contains(newFilter)) {
				matchClone.put(o, TRUE_BOOL);
				recurse(o, provider, true);
			} else {
				// if I don't match, then if ANY of my children match, I also match
				Object[] children = provider.getChildren(o);
				for( int i = 0; i < children.length; i++ ) 
					if( !canceled ) 
						fullCache(children[i], provider);

				if( found ) {
					showClone.put(o, TRUE_BOOL);
					found = true;
				}
			}
		}
		
		protected void recurse(Object o, ITreeContentProvider provider, boolean match) {
			Object[] children = provider.getChildren(o);
			for( int i = 0; i < children.length; i++ ) {
				if( match )
					showClone.put(children[i], TRUE_BOOL);
				else
					showClone.remove(children[i]);
				recurse(children[i], provider, match);
			}
			Object parent = provider.getParent(o);
			if( match ) {
				while( parent != null ) {
					showClone.put(parent, TRUE_BOOL);
					parent = provider.getParent(parent);
				}
			} else {
				while( parent != null ) {
					showClone.remove(parent);
					parent = provider.getParent(parent);
				}
			}
		}

		
		protected void refine() {
			ITreeContentProvider provider = (ITreeContentProvider)navigator.getCommonViewer().getContentProvider();
			Iterator i = matchClone.keySet().iterator();
			Set<Object> toRemove = new HashSet<Object>();
			Set<Object> mustRemain = new HashSet<Object>();
			
			Object o;
			String elementAsString;

			while(i.hasNext() && !canceled) {
				o = i.next();
				elementAsString = MBeanExplorerLabelProvider.getText2(o);
				if( !elementAsString.contains(newFilter)) {
					toRemove.add(o);
				} else {
					mustRemain.add(o);
				}
			}
			
			for( Object o2 : toRemove ) {
				matchClone.remove(o2);
			}
			showClone = new HashMap<Object, Boolean>();
			for( Object o2 : mustRemain ) {
				recurse(o2, provider, true);
			}
		}
		
		public void cancel() {
			canceled = true;
		}
	}
	
	protected synchronized void threadFinished(
			HashMap<Object, Boolean> newMatches, 
			HashMap<Object, Boolean> newShow, String filter) {
		matches = newMatches;
		shouldShow = newShow;
		filterText = filter;
		refineThread = null;
		refreshView();
	}
	
	public boolean shouldShow(Object element, Object parentElement) {
		return matches == null || matches.containsKey(element)
			|| shouldShow.containsKey(element);
	}
	
    public void dispose() {
    }    
}
