/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

/**
 * @author Andre Dietisheim
 */
public class RegExUtils {

	public static String escapeRegex(String value) {
		StringBuilder builder = new StringBuilder();
		for(char character : value.toCharArray()) {
			if ('/' == character) {
				builder.append('\\');
			} 
			builder.append(character);
		}
		return builder.toString();
	}
	
}
