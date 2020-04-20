/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.reddeer;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class RuntimeMatcher extends TypeSafeMatcher<Runtime>{

	private Runtime expected;
	
	public RuntimeMatcher(Runtime expected) {
		this.expected = expected;
	}
	
	@Override
	public boolean matchesSafely(Runtime item) {
		return expected.equals(item);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expected);
	}
}
