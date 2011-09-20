/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertEquals;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class JsonSanitizerTest {

	@Test
	public void canSanitizeQuotedJsonObject() throws OpenshiftException {
		String quotedJsonObject =
				"\"{"
						+ "\\\"carts\\\":"
						+ "\\\"perl-5.10\\\""
						+ "}\"";

		String sanitizedJson = JsonSanitizer.sanitize(quotedJsonObject);
		assertEquals("{\"carts\":\"perl-5.10\"}", sanitizedJson);
	}

	@Test
	public void doesNotTuchValidJson() throws OpenshiftException {
		String quotedJsonObject =
				"{"
						+ "\"carts\":"
						+ "\"perl-5.10\""
						+ "}";

		String sanitizedJson = JsonSanitizer.sanitize(quotedJsonObject);
		assertEquals("{\"carts\":\"perl-5.10\"}", sanitizedJson);
	}

	@Test
	public void doesNotRemoveEscapedQuoteInStringValue() throws OpenshiftException {
		String quotedJsonObject =
				"{"
						+ "\"property\":"
						+ "\"stringWithA\\\"Quote\""
						+ "}";

		String sanitizedJson = JsonSanitizer.sanitize(quotedJsonObject);
		assertEquals("{\"property\":\"stringWithA\\\"Quote\"}", sanitizedJson);
		ModelNode node = ModelNode.fromJSONString(sanitizedJson);
		assertEquals("stringWithA\"Quote", node.get("property").asString());
	}

}
