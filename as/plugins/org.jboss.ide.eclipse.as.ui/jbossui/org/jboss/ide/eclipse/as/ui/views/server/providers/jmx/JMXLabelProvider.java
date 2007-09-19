package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXAttributesWrapper;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXDomain;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXException;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;

class JMXLabelProvider extends LabelProvider {
	public String getText(Object obj) {
		if (obj instanceof JMXDomain)
			return ((JMXDomain) obj).getName();
		if (obj instanceof JMXBean) {
			return ((JMXBean) obj).getName().substring(
					((JMXBean) obj).getDomain().length() + 1);
		}
		if (obj instanceof WrappedMBeanOperationInfo) {
			return ((WrappedMBeanOperationInfo) obj).getInfo().getName();
		}

		if (obj instanceof JMXException) {
			String message = "";
			message += ((JMXException) obj).getException().getClass()
					.getName()
					+ ": ";
			message += ((JMXException) obj).getException().getMessage();
			return message;
		}
		if (obj instanceof JMXAttributesWrapper) {
			return "Attributes";
		}
		if (obj == JMXViewProvider.LOADING)
			return "loading...";
		return "not sure yet: " + obj.getClass().getName();
	}

	public Image getImage(Object obj) {
		if (obj instanceof JMXException) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJS_ERROR_TSK);
		}
		if (obj instanceof WrappedMBeanOperationInfo)
			return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
		if (obj instanceof JMXAttributesWrapper) {
			return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
		}

		return null;
	}

}
