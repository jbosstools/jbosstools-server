/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.dialogs;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;

public class AttributesPanel extends Composite {
	private Attributes attr;
	private String title;
	private Map<String, Object> values;

	public AttributesPanel(Composite parent, int style, Attributes attributes, String title,
			Map<String, Object> values) {
		super(parent, style);
		this.attr = attributes;
		this.title = title;
		this.values = values;
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setLayout(new GridLayout(1, false));
		Group g = new Group(this, style);
		g.setText(title);
		g.setLayout(new GridLayout(1, false));
		g.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Map<String, Attribute> map = attributes.getAttributes();
		for (String key : map.keySet()) {
			Attribute oneAttribute = map.get(key);
			Composite c = new AttributePanel(g, SWT.NONE, key, oneAttribute, values);
		}
	}
}
