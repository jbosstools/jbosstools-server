/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.util.composites;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class FilesetPreviewComposite extends Composite  {
	private TableViewer previewTable;
	public FilesetPreviewComposite (Composite parent, int style) {
		super(parent, style);
		previewTable = new TableViewer(this, SWT.BORDER);
		previewTable.setContentProvider(new ArrayContentProvider());
		previewTable.setLabelProvider(new ResourceLabelProvider());
		
		setLayout(new FormLayout());
		FormData data = new FormData();
		data.left = new FormAttachment(0,5);
		data.right = new FormAttachment(100,-5);
		data.top = new FormAttachment(0,5);
		data.bottom = new FormAttachment(100,-5);
		data.height = 200;
		previewTable.getTable().setLayoutData(data);
	}
	
	public FilesetPreviewComposite (Composite parent)
	{
		this(parent, SWT.NONE);
	}

	public void setInput(Object[] o) {
		previewTable.setInput(o);
	}
	public void setEnabled(boolean bool) {
		previewTable.getTable().setEnabled(bool);
	}
	public void clearAll() {
		previewTable.getTable().clearAll();
	}

	private class ResourceLabelProvider implements ILabelProvider
	{

		public Image getImage(Object element) {
			if (element instanceof IResource)
			{
				IResource resource = (IResource) element;
				if (resource.getType() == IResource.PROJECT)
				{
					return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
				}
				else if (resource.getType() == IResource.FOLDER)
				{
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				}
				else if (resource.getType() == IResource.FILE)
				{
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
				}
			} else if (element instanceof IPath) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
			return null;
		}

		public String getText(Object element) {
			return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {}

		public void dispose() {}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) { }
		
	}
}
