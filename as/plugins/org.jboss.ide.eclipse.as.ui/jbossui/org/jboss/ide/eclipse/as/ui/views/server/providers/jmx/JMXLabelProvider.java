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

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
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
