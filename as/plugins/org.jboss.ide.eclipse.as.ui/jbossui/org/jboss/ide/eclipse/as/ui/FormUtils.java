/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Utility methods that help in dealing with eclipse forms.
 * 
 * @author André Dietisheim
 * 
 */
public class FormUtils {

	public static void adaptFormCompositeRecursively(Composite composite, FormToolkit toolkit) {
		if (FormUtils.isInSection(composite)) {
			FormUtils.adaptRecursively(composite, toolkit);
		}
	}

	public static boolean isInSection(Composite composite) {
		return composite.getParent() instanceof Section;
	}

	public static void adaptRecursively(Composite composite, FormToolkit toolkit) {
		toolkit.adapt(composite);
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			if (control instanceof Composite) {
				adaptRecursively((Composite) control, toolkit);
			} else {
				toolkit.adapt(control, true, false);
			}
		}
	}

}
