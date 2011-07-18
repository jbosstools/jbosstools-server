package org.jboss.ide.eclipse.as.core.util;

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
