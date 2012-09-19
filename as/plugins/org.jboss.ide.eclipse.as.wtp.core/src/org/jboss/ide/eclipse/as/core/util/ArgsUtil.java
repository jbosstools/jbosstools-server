/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

public class ArgsUtil {

	public static final Integer NO_VALUE = new Integer(-1); 
	public static final String EQ = "="; //$NON-NLS-1$
	public static final String SPACE=" "; //$NON-NLS-1$
	public static final String VMO = "-D"; //$NON-NLS-1$
	public static final String EMPTY=""; //$NON-NLS-1$
	public static final String QUOTE="\""; //$NON-NLS-1$
	
	public static String[] parse(String s) {
		if( s == null )
			return new String[0];
		s = s.trim();
		
		try {
			ArrayList<String> l = new ArrayList<String>();
			int length = s.length();
			
			int current = 0;
			boolean inQuotes = false;
			boolean done = false;
			String tmp = EMPTY;
			StringBuffer buf = new StringBuffer();
			if( s.length() == 0 )
				done = true;
			while( !done ) {
				switch(s.charAt(current)) {
				case '"':
					inQuotes = !inQuotes;
					buf.append(s.charAt(current));
					break;
				case '\n':
				case ' ':
					if( !inQuotes ) {
						tmp = buf.toString();
						if( !(tmp.trim()).equals("")) //$NON-NLS-1$
							l.add(tmp);
						buf = new StringBuffer();
					} else {
						buf.append(' ');
					}
					break;
				default:
					buf.append(s.charAt(current));
					break;
				}
				current++;
				if( current == length ) done = true;
			}
			
			l.add(buf.toString());
			
			Object[] lArr = l.toArray();
			String[] retVal = new String[lArr.length];
			for( int i = 0; i < lArr.length; i++ ) {
				retVal[i] = (String)lArr[i];
			}
			return retVal;
		} catch( Exception e ) {
			Status status = new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 
					NLS.bind(Messages.ServerArgsParseError, s), e);
			ASWTPToolsPlugin.log(status);
			return new String[] { };
		}
	}

	public static Map<String, Object> getSystemProperties(String argString) {
		String[] args = parse(argString);
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].startsWith(VMO)) {
				int eq = args[i].indexOf(EQ);
				if( eq != -1 ) {
					map.put(args[i].substring(2, eq), 
							args[i].substring(eq+1));
				} else {
					map.put(args[i], NO_VALUE);
				}
			}
		}
		return map;
	}

	public static String getValue(String allArgs, String shortOpt, String longOpt) {
		return getValue(parse(allArgs), shortOpt, longOpt);
	}
	
	public static String getValue(String[] args, String shortOpt, String longOpt ) {
		String[] shortOpt2 = shortOpt == null ? new String[0] : new String[]{shortOpt};
		String[] longOpt2 = longOpt == null ? new String[0] : new String[]{longOpt};
		return getValue(args,shortOpt2,longOpt2);
	}

	public static String getValue(String allArgs, String[] shortOpt, String[] longOpt) {
		return getValue(parse(allArgs), shortOpt, longOpt);
	}
	
	public static String getValue(String[] args, String[] shortOpt, String[] longOpt ) {
		for( int i = 0; i < args.length; i++ ) {
			if( shortOpt != null && matchesShortArg(args[i], shortOpt) && i+1 < args.length)
				return args[i+1];
			if( longOpt != null && matchesLongArg(args[i], longOpt)) 
				return args[i].substring(args[i].indexOf(EQ) + 1);
		}
		return null;
	}

	public static boolean matchesShortArg(String needle, String[] haystack) {
		if( haystack == null )
			return false;
		return Arrays.asList(haystack).contains(needle);
	}
	
	public static boolean matchesLongArg(String needle, String[] haystack) {
		if( haystack == null )
			return false;
		for( int i = 0; i < haystack.length; i++ ) {
			if( needle.startsWith(haystack[i] + EQ) || needle.startsWith(QUOTE + haystack[i] + EQ))
				return true;
		}
		return false;
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value ) {
		if( value != null && value.contains(SPACE)) {
			// avoid double quotes
			if( !(value.startsWith(QUOTE) && value.endsWith(QUOTE)))
				value = QUOTE + value + QUOTE;
		}
		return setArg(allArgs, shortOpt, longOpt, value, false);
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value, boolean addQuotes ) {
		String[] shortOpt2 = shortOpt == null ? new String[0] : new String[]{shortOpt};
		String[] longOpt2 = longOpt == null ? new String[0] : new String[]{longOpt};
		return setArg(allArgs, shortOpt2, longOpt2, value, addQuotes);
	}
	
	/**
	 * Replace (or add) an argument. 
	 * Parse through the "allArgs" parameter to create a list of arguments.
	 * Compare each element in allArgs until you find a match against 
	 * one of the short argument (-b value) or long argument (--host=etcetcetc)
	 * patterns. The set of short and long form arguments should be 100% interchangeable,
	 * and the caller must not have a preference which is ultimately returned. 
	 * 
	 * If a match is found, and the match is in the short-form arguments, 
	 * do not change the arg (-b value), but update the value in the next segment. 
	 * 
	 * If a match is found and it is a long form argument, replace the string
	 * (ex:  --host=localhost) with the first longOpt (--newLongOpt=127.0.0.1)
	 * 
	 * @param allArgs
	 * @param shortOpt
	 * @param longOpt An array of possible long-form options
	 * @param value The new value, or null if you want to clear the option
	 * @param addQuotes
	 * @return
	 */
	public static String setArg(String allArgs, String[] shortOpt, String[] longOpt, String value, boolean addQuotes ) {
		String originalValue = value;
		String rawValue = originalValue;
		if( value != null && addQuotes )
			value = QUOTE + value + QUOTE;
		else 
			rawValue = getRawValue(value);

		boolean found = false;
		String[] args = parse(allArgs);
		String retVal = EMPTY;
		for( int i = 0; i < args.length; i++ ) {
			if( matchesShortArg(args[i], shortOpt)) {
				if( value != null ) {
					args[i+1] = value;
					retVal += args[i] + SPACE + args[++i] + SPACE;
				}
				found = true;
			} else if(  matchesLongArg(args[i], longOpt)) {
				if( value != null ) {
					String newVal = null;
					if( args[i].startsWith(QUOTE)) {
						newVal = QUOTE + longOpt[0] + EQ + rawValue + QUOTE;
					} else {
						newVal = longOpt[0] + EQ + value;
					}
					args[i] = newVal;
					retVal += args[i] + SPACE;
				}
				found = true;
			} else {
				retVal += args[i] + SPACE;
			}
		}
		
		// turn this to a retval;
		if( !found ) {
			if( longOpt != null && longOpt.length > 0 ) 
				retVal = retVal + longOpt[0] + EQ + value;
			else
				retVal = retVal + shortOpt[0] + SPACE + value;
		} 
		return retVal;
	}
	
	private static String getRawValue(String original) {
		if( original != null && original.startsWith(QUOTE) && original.endsWith(QUOTE)) {
			original = original.substring(1);
			original = original.substring(0, original.length()-1);
		}
		return original;
	}
	
	public static String setFlag(String original, String flagName) {
		if( original.startsWith(flagName + SPACE ) || original.contains(SPACE + flagName + SPACE) || original.endsWith(flagName)) 
			return original;
		return original.trim() + SPACE + flagName;
	}
	
	public static String clearFlag(String original, String flagName) {
		if( original.trim().startsWith(flagName + SPACE))
			return original.trim().substring(flagName.length()).trim();
		
		if( original.contains(SPACE + flagName + SPACE)) { 
			return original.replace(SPACE + flagName + SPACE, SPACE).trim();
		}
		
		if( original.trim().endsWith(SPACE + flagName)) {
			return original.trim().substring(0, original.trim().length() - flagName.length()).trim();
		}
		if( original.trim().equals(flagName)) {
			return ""; //$NON-NLS-1$
		}
		return original.trim();
	}
}