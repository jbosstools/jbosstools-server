package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

/**
 * This class coppied from JBoss's DMR project, ExpressionValue.java
 *
 */
public class ExpressionResolverUtil {

	/*
	 * An interface is added to allow the actual resolving of the variables
	 * to be customized
	 */
	public interface IExpressionResolver {
		public String getSystemProperty(String variable);
		public String getEnvironmentProperty(String variable);
	}
	
	public static class NullExpressionResolver implements IExpressionResolver {
		public String getSystemProperty(String variable) {
			return null;
		}
		public String getEnvironmentProperty(String variable) {
			return null;
		}
	}
	
    private static final int INITIAL = 0;
    private static final int GOT_DOLLAR = 1;
    private static final int GOT_OPEN_BRACE = 2;
    private static final int RESOLVED = 3;
    private static final int DEFAULT = 4;

    public static String safeReplaceProperties(final String value) {
    	return safeReplaceProperties(value, null);
    }
    public static String safeReplaceProperties(final String value, IExpressionResolver resolver) {
    	try {
    		return replaceProperties(value, resolver);
    	} catch(IllegalStateException ise) {
    		return value; // Just return the string unchanged
    	}
    }
    
    /**
     * Replace properties of the form:
     * <code>${<i>&lt;[env.]name&gt;[</i>,<i>&lt;[env.]name2&gt;[</i>,<i>&lt;[env.]name3&gt;...]][</i>:<i>&lt;default&gt;]</i>}</code>
     *
     * @param value - either a system property or environment variable reference
     * @param resolver - to resolve the variables
     * @return the value of the system property or environment variable referenced if
     *  it exists
     */
    public static String replaceProperties(final String value, IExpressionResolver resolver) {
    	if( resolver == null )
    		resolver = new NullExpressionResolver();
    	
        final StringBuilder builder = new StringBuilder();
        final int len = value.length();
        int state = INITIAL;
        int start = -1;
        int nameStart = -1;
        String resolvedValue = null;
        for (int i = 0; i < len; i = value.offsetByCodePoints(i, 1)) {
            final int ch = value.codePointAt(i);
            switch (state) {
                case INITIAL: {
                    switch (ch) {
                        case '$': {
                            state = GOT_DOLLAR;
                            continue;
                        }
                        default: {
                            builder.appendCodePoint(ch);
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_DOLLAR: {
                    switch (ch) {
                        case '$': {
                            builder.appendCodePoint(ch);
                            state = INITIAL;
                            continue;
                        }
                        case '{': {
                            start = i + 1;
                            nameStart = start;
                            state = GOT_OPEN_BRACE;
                            continue;
                        }
                        default: {
                            // invalid; emit and resume
                            builder.append('$').appendCodePoint(ch);
                            state = INITIAL;
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_OPEN_BRACE: {
                    switch (ch) {
                        case ':':
                        case '}':
                        case ',': {
                            final String name = value.substring(nameStart, i).trim();
                            if ("/".equals(name)) { //$NON-NLS-1$
                                builder.append(File.separator);
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (":".equals(name)) { //$NON-NLS-1$
                                builder.append(File.pathSeparator);
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            }
                            // First check for system property, then env variable
                            String val = resolver.getSystemProperty(name);
                            if (val == null && name.startsWith("env.")) //$NON-NLS-1$
                                val = resolver.getEnvironmentProperty(name.substring(4));

                            if (val != null) {
                                builder.append(val);
                                resolvedValue = val;
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (ch == ',') {
                                nameStart = i + 1;
                                continue;
                            } else if (ch == ':') {
                                start = i + 1;
                                state = DEFAULT;
                                continue;
                            } else {
                                throw new IllegalStateException("Failed to resolve expression: "+ value.substring(start - 2, i + 1)); //$NON-NLS-1$
                            }
                        }
                        default: {
                            continue;
                        }
                    }
                    // not reachable
                }
                case RESOLVED: {
                    if (ch == '}') {
                        state = INITIAL;
                    }
                    continue;
                }
                case DEFAULT: {
                    if (ch == '}') {
                        state = INITIAL;
                        builder.append(value.substring(start, i));
                    }
                    continue;
                }
                default:
                    throw new IllegalStateException("Unexpected char seen: "+ch); //$NON-NLS-1$
            }
        }
        switch (state) {
            case GOT_DOLLAR: {
                builder.append('$');
                break;
            }
            case DEFAULT: {
                builder.append(value.substring(start - 2));
                break;
            }
            case GOT_OPEN_BRACE: {
                // We had a reference that was not resolved, throw ISE
                if (resolvedValue == null)
                    throw new IllegalStateException("Incomplete expression: "+builder.toString()); //$NON-NLS-1$
                break;
           }
        }
        return builder.toString();
    }
}
