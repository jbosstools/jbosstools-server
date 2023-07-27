/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Convert a long command string into array items suitable to be passed into
 * Runtime.exec(etc) in order to spawn a process.
 */
public class CommandLineUtils {
    public static String[] translateCommandline( String toProcess )  throws CommandLineException {
        if ( ( toProcess == null ) || ( toProcess.length() == 0 ) ) {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer( toProcess, "\"\' ", true );
        Vector<String> v = new Vector<String>();
        StringBuilder current = new StringBuilder();

        while ( tok.hasMoreTokens() ) {
            String nextTok = tok.nextToken();
            switch ( state ) {
                case inQuote:
                    if ( "\'".equals( nextTok ) ) {
                        state = normal;
                    } else {
                        current.append( nextTok );
                    }
                    break;
                case inDoubleQuote:
                    if ( "\"".equals( nextTok ) ) {
                        state = normal;
                    } else {
                        current.append( nextTok );
                    }
                    break;
                default:
                    if ( "\'".equals( nextTok ) ) {
                        state = inQuote;
                    } else if ( "\"".equals( nextTok ) ) {
                        state = inDoubleQuote;
                    } else if ( " ".equals( nextTok ) ) {
                        if ( current.length() != 0 ) {
                            v.addElement( current.toString() );
                            current.setLength( 0 );
                        }
                    }  else  {
                        current.append( nextTok );
                    }
                    break;
            }
        }

        if ( current.length() != 0 ) {
            v.addElement( current.toString() );
        }

        if ( ( state == inQuote ) || ( state == inDoubleQuote ) ) {
            throw new CommandLineException( "unbalanced quotes in " + toProcess );
        }

        String[] args = new String[v.size()];
        v.copyInto( args );
        return args;
    }
    public static class CommandLineException extends Exception {
        public CommandLineException(String s) {
            super(s);
        }
    }
}
