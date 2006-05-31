package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RuntimeConfigUtil {

	public static final Integer NO_VALUE = new Integer(-1); 
	/**
	 * This class will find command line parameters. For example:
	 * 
	 *    getCommandArgument("-b localhost --configuration=minimal", "-b", "--host") 
	 *  will return "localhost".
	 *  
	 *    getCommandArgument("-b localhost --configuration=minimal", "-c", "--configuration") 
	 *  will return "minimal".
	 *  
	 *  
	 *  It's very simple and probably won't work in all cases. 
	 *  It is not extensively tested. 
	 * @param args
	 * @param primaryPrefix
	 * @param secondaryPrefix
	 * @return
	 */
	public static String getCommandArgument(String args, 
			String primaryPrefix, String secondaryPrefix) {
	
		String[] argArray = parse(args);
		
		// system property is different. Do them first.
		if( primaryPrefix.startsWith("-D")) {
			for( int i = 0; i < argArray.length; i++ ) {
				if( argArray[i].startsWith(primaryPrefix)) {
					int eqIndex = argArray[i].indexOf('=');
					if( eqIndex != -1 ) {
						return argArray[i].substring(eqIndex+1);
					} else if( argArray[i].equals(primaryPrefix)){
						// no value
						return "";
					}
					// searching for -Dparam, found -DparamOther
					// keep searching
				}
			}
		}
		
		for( int i = 0; i < argArray.length; i++ ) {

			// -c config
			if( argArray[i].equals(primaryPrefix)) {
				if( i+1 <= argArray.length) {
					return argArray[i+1];
				} else {
					return "";
				}
			}
			
			if( argArray[i].startsWith(secondaryPrefix )) {
				int eqIndex = argArray[i].indexOf('=');
				if( eqIndex != -1 ) {
					return argArray[i].substring(eqIndex+1);
				} 
				return "";
			}
		}
		return "";
	}
	
	public static Map getSystemProperties(String s) {
		String[] args = parse(s);
		HashMap map = new HashMap();
		
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].startsWith("-D")) {
				int eq = args[i].indexOf('=');
				if( eq != -1 ) {
					map.put(args[i].substring(0, eq), 
							args[i].substring(eq+1));
				} else {
					map.put(args[i], NO_VALUE);
				}
			}
		}
		
		return map;
		
	}

	public static String[] parse(String s) {
		ArrayList l = new ArrayList();
		int length = s.length();
		
		int start = 0;
		int current = 0;
		
		boolean inQuotes = false;
		boolean escaped = false;
		
		boolean done = false;
		String tmp = "";
		StringBuffer buf = new StringBuffer();

		while( !done ) {
			switch(s.charAt(current)) {
			case '\\':
				current++;
				buf.append(s.charAt(current));
				break;
			case '"':
				inQuotes = !inQuotes;
				break;
			case ' ':
				if( !inQuotes ) {
					tmp = buf.toString();
					l.add(tmp);
					start = current+1;
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
			if( current == s.length() ) done = true;
		}
		
		l.add(buf.toString());
		
		Object[] lArr = l.toArray();
		String[] retVal = new String[lArr.length];
		for( int i = 0; i < lArr.length; i++ ) {
			retVal[i] = (String)lArr[i];
		}
		return retVal;
	}

	

}
