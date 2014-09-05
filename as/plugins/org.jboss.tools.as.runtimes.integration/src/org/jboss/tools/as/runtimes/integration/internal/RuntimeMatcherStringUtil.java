/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

public class RuntimeMatcherStringUtil {
	public static String getSafeVersionString(String version) {
		int thirdDot = nthOccurrence(version, '.', 3);
		if( thirdDot == -1 )
			return version;
		String substr = version.substring(thirdDot + 1);
		String substrReplaced = substr.replace('.', '_');
		String safeVersion  = version.substring(0, thirdDot+1) + substrReplaced;
		return safeVersion;
	}
	
	public static int nthOccurrence(String str, char c, int n) {
		if( n == 0 ) // no such thing as zero-th occurance
			return -1;
		n--;
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}

}
