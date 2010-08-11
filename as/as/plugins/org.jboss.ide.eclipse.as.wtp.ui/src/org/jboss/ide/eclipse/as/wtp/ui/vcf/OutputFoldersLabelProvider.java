package org.jboss.ide.eclipse.as.wtp.ui.vcf;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.jboss.ide.eclipse.as.wtp.core.vcf.OutputFoldersVirtualComponent;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.IVirtualComponentLabelProvider;

public class OutputFoldersLabelProvider implements IVirtualComponentLabelProvider {
	public OutputFoldersLabelProvider() {
	}

	public boolean canHandle(IVirtualComponent component) {
		return component instanceof OutputFoldersVirtualComponent;
	}

	public Image getSourceImage(IVirtualComponent component) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	public String getSourceText(IVirtualComponent component) {
		//OutputFoldersVirtualComponent fileset = (OutputFoldersVirtualComponent)component;
		return component.getProject().getName() + Messages.OutputFolders;
	}


}
