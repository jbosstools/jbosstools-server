/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.commons.ui.label;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.commons.tree.HasOwner;
import org.jboss.tools.jmx.commons.ImageProvider;
import org.jboss.tools.jmx.commons.ui.propsrc.BeanPropertySource;
import org.jboss.tools.jmx.commons.util.Function1;


public class ImageLabelProvider extends FunctionColumnLabelProvider {

	public ImageLabelProvider(Function1 function) {
		super(function);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ImageProvider) {
			ImageProvider ip = (ImageProvider) element;
			return ip.getImage();
		}
		if (element instanceof HasOwner) {
			HasOwner ho = (HasOwner) element;
			Object bean = ho.getOwner();
			if (bean instanceof ImageProvider) {
				ImageProvider ip = (ImageProvider) bean;
				return ip.getImage();
			}
		}
		if (element instanceof BeanPropertySource) {
			BeanPropertySource bps = (BeanPropertySource) element;
			Object bean = bps.getBean();
			if (bean instanceof ImageProvider) {
				ImageProvider ip = (ImageProvider) bean;
				return ip.getImage();
			}
		}
		return super.getImage(element);
	}


}
