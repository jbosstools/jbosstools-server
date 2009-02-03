/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArgsUtil {

	public static final Integer NO_VALUE = new Integer(-1); 

	public static Map<String, Object> getSystemProperties(String argString) {
		String[] args = parse(argString);
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].startsWith("-D")) {
				int eq = args[i].indexOf('=');
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
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].equals(shortOpt) && i+1 < args.length)
				return args[i+1];
			if( longOpt != null && args[i].startsWith(longOpt + "=")) 
				return args[i].substring(args[i].indexOf('=') + 1);
		}
		return null;
	}
	
	public static String[] parse(String s) {
		try {
			ArrayList<String> l = new ArrayList<String>();
			int length = s.length();
			
			int current = 0;
			boolean inQuotes = false;
			boolean done = false;
			String tmp = "";
			StringBuffer buf = new StringBuffer();
	
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
			return new String[] { };
		}
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value ) {
		if( value.contains(" "))
			value = "\"" + value + "\"";
		return setArg(allArgs, shortOpt, longOpt, value, false);
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value, boolean addQuotes ) {
		if( addQuotes ) 
			value = "\"" + value + "\"";
		String[] args = parse(allArgs);
		String retVal = "";
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].equals(shortOpt)) {
				args[i+1] = value;
				retVal += args[i] + " " + args[++i] + " ";
			} else if( longOpt != null && args[i].startsWith(longOpt + "=")) { 
				args[i] = longOpt + "=" + value;
				retVal += args[i] + " ";
			} else {
				retVal += args[i] + " ";
			}
		}
		
		// turn this to a retval;
		return retVal;
	}
	
}