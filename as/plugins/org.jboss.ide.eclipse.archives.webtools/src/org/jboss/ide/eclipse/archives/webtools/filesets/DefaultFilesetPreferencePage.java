/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.as.ui.preferences.ServerTypePreferencePage;

public class DefaultFilesetPreferencePage extends ServerTypePreferencePage {
	private FilesetPreferenceComposite rootComp;
	@Override
	protected Control createContents(Composite parent) {
		rootComp = new FilesetPreferenceComposite(parent, SWT.NONE);
		rootComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComp.layout();
		return rootComp;
	}
	
	public boolean performOk() {
		String[] changed2 = rootComp.getChanged();
		ArrayList<Object> list;
		Fileset[] arr;
		for( int i = 0; i < changed2.length; i++ ) {
			list = rootComp.getDataForServer(changed2[i]);
			arr = (Fileset[]) list.toArray(new Fileset[list.size()]);
			IPath fileToWrite = FilesetUtil.DEFAULT_FS_ROOT.append(changed2[i]);
			FilesetUtil.saveFilesets(fileToWrite.toFile(), arr);
		}
		rootComp.clearChanged();
	    return true;
	} 

	public static class FilesetPreferenceComposite extends ServerTypePreferenceComposite {

		public FilesetPreferenceComposite(Composite parent, int style) {
			super(parent, style);
		}
		public String getDescriptionLabel() {
			return Messages.DefaultFilesetsLabel;
		}
		protected void addPressed() {
			FilesetDialog d = new FilesetDialog(addButton.getShell(), "", null); //$NON-NLS-1$
			d.setShowViewer(false);
			if( d.open() == Window.OK) {
				Fileset fs = d.getFileset();
				addObject(fs);
			}
		}
	
		protected LabelProvider getLabelProvider() {
			return new FilesetLabelProvider();
		}
		
		protected void initializeDataModel() {
			super.initializeDataModel();
		}

		protected Object[] getCurrentServerDataModel() {
			return getCurrentServerSets();
		}
		
		protected Fileset[] getCurrentServerSets() {
			String id = getCurrentId();
			ArrayList<Object> list = new ArrayList<Object>();
			if( id != null ) {
				list = getDataForServer(id);
				if( list == null ) {
					IPath fileToRead = FilesetUtil.DEFAULT_FS_ROOT.append(id);
					Fileset[] sets = FilesetUtil.loadFilesets(fileToRead.toFile(), null);
					list = new ArrayList<Object>();
					list.addAll(Arrays.asList(sets));
					cacheMap.put(id, list);
				}
			}
			return (Fileset[]) list.toArray(new Fileset[list.size()]);
		}

	} // end inner class
}
