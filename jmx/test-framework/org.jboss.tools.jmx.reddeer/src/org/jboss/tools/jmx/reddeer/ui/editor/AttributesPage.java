/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.reddeer.ui.editor;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.uiforms.impl.section.DefaultSection;

/**
 * 
 * @author odockal
 *
 */
public class AttributesPage extends MBeanEditorPage {

	public AttributesPage(ReferencedComposite composite) {
		super(composite);
	}

	@Override
	public DefaultSection getAllSection() {
		return new DefaultSection("All Attributes");
	}

	@Override
	public DefaultSection getDetailsSection() {
		return new DefaultSection("Attribute Details");
	}
	
	@Override
	protected int getProperIndex() {
		return getTable().getHeaderIndex("Name");
	}
}