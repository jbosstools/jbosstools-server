package org.jboss.ide.eclipse.as.wtp.ui.propertypage;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

public interface IVirtualComponentLabelProvider {
	public boolean canHandle(IVirtualComponent component);
	public String getSourceText(IVirtualComponent component);
	public Image getSourceImage(IVirtualComponent component);
}
