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
package org.jboss.ide.eclipse.archives.webtools.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.archives.webtools.ui.FilesetContentProvider.FolderWrapper;
import org.jboss.ide.eclipse.archives.webtools.ui.FilesetContentProvider.PathWrapper;
import org.jboss.ide.eclipse.archives.webtools.ui.FilesetContentProvider.ServerWrapper;

public class FilesetLabelProvider extends LabelProvider {

    private LocalResourceManager resourceManager;
    private Image rootImage;
	public FilesetLabelProvider() {
		super();
		this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
		ImageDescriptor des = ImageDescriptor.createFromURL(IntegrationPlugin.getDefault().getBundle().getEntry("icons/multiple_files.gif")); //$NON-NLS-1$
		rootImage = des.createImage();
	}

	public Image getImage(Object element) {
    	if( element instanceof Fileset ) {
    		return PlatformUI.getWorkbench().getSharedImages()
            .getImage(ISharedImages.IMG_OBJ_FOLDER);
    	} else if( element instanceof FolderWrapper ) {
    		return PlatformUI.getWorkbench().getSharedImages()
            .getImage(ISharedImages.IMG_OBJ_FOLDER);
    	} else if( element instanceof PathWrapper ) {
	    	String fileName = ((PathWrapper)element).getPath().toOSString();
	    	IContentTypeManager manager = Platform.getContentTypeManager();
	    	IContentTypeMatcher matcher = manager.getMatcher(null, null);
	    	IContentType contentType = matcher.findContentTypeFor(fileName);
	        ImageDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry()
	        	.getImageDescriptor(fileName, contentType);
		    if (descriptor == null) {
		    	descriptor = PlatformUI.getWorkbench().getSharedImages()
		                .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
			}
		    return resourceManager.createImage(descriptor);
    	} else if( element instanceof ServerWrapper ) {
    		return rootImage;
    	}
        return null;
    }

    public String getText(Object element) {
    	if( element instanceof PathWrapper ) return ((PathWrapper)element).getLocalizedResourceName();
    	if( element instanceof Fileset ) return ((Fileset)element).getName() + "  " + ((Fileset)element).getRawFolder(); //$NON-NLS-1$
    	if( element instanceof ServerWrapper ) return "Filesets"; //$NON-NLS-1$
        return null;
    }


	public void dispose() {
		resourceManager.dispose();
		resourceManager = null;
		rootImage.dispose();
		super.dispose();
	}

}
