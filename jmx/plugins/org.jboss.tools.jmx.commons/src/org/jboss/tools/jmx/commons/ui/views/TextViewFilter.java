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

package org.jboss.tools.jmx.commons.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.jmx.commons.util.TextFilter;
import org.jboss.tools.jmx.commons.util.TextFilters;


/**
 * A {@link ViewerFilter} for elements implementing {@link TextFilter}
 */
public class TextViewFilter extends ViewerFilter {

	private String searchText;

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return TextFilters.matches(searchText, element);
	}

}
