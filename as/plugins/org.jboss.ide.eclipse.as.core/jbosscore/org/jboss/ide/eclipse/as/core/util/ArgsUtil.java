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

	public static Map getSystemProperties(String argString) {
		String[] args = parse(argString);
		HashMap map = new HashMap();
		
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
		String[] args = parse(allArgs);
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].equals(shortOpt))
				return args[i+1];
			if( args[i].startsWith(longOpt + "=")) 
				return args[i].substring(args[i].indexOf('=') + 1);
		}
		return null;
	}
	public static String[] parse(String s) {
		try {
			ArrayList l = new ArrayList();
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
}