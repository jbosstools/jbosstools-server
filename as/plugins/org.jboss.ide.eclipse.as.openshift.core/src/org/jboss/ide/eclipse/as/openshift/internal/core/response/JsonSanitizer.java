package org.jboss.ide.eclipse.as.openshift.internal.core.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSanitizer {

	private static final Pattern QUOTED_JSON_OBJECT_PATTERN = Pattern.compile("\"\\{(.+)\\}\"");
	private static final Pattern ESCAPED_QUOTES_PATTERN = Pattern.compile("\\\"");

	public static String sanitize(String json) {
		return correctEscapedJsonObjects(json);
	}

	/**
	 * Corrects erroneously quoted json objects in the given string.
	 * <p>
	 * "{ \"property\": \"value\" }"
	 * 
	 * @param json
	 * @return
	 */
	protected static String correctEscapedJsonObjects(String json) {
		String sanitizedJson = json;
		Matcher matcher = QUOTED_JSON_OBJECT_PATTERN.matcher(json);
		if (matcher.find()
				&& matcher.groupCount() > 0) {
			sanitizedJson = matcher.replaceAll("{" + unescapeQuotes(matcher.group(1)) + "}");
		}
		return sanitizedJson;
	}

	private static String unescapeQuotes(String responseFragment) {
		return ESCAPED_QUOTES_PATTERN.matcher(responseFragment).replaceAll("\"");
	}

}
