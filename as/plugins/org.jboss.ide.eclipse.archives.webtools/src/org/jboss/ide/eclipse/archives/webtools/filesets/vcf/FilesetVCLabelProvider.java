package org.jboss.ide.eclipse.archives.webtools.filesets.vcf;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.ui.internal.propertypage.IVirtualComponentLabelProvider;
import org.jboss.ide.eclipse.archives.webtools.SharedImages;

public class FilesetVCLabelProvider implements IVirtualComponentLabelProvider {

	public FilesetVCLabelProvider() {
	}

	public boolean canHandle(IVirtualComponent component) {
		return component instanceof WorkspaceFilesetVirtualComponent;
	}

	public Image getSourceImage(IVirtualComponent component) {
		return SharedImages.getImage(SharedImages.FILESET_IMAGE);
	}

	public String getSourceText(IVirtualComponent component) {
		WorkspaceFilesetVirtualComponent fileset = (WorkspaceFilesetVirtualComponent)component;
		String base = fileset.getRootFolderPath();
		if( fileset.getIncludes() != null && !fileset.getIncludes().equals(""))  //$NON-NLS-1$
			base += " [" + fileset.getIncludes() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		if( fileset.getExcludes() != null && !fileset.getExcludes().equals(""))  //$NON-NLS-1$
			base += " [" + fileset.getExcludes() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		return base;
	}

	public void dispose() {
	}
}
